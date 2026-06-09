package gui.model;

import database.dao.AppointmentDao;
import database.dao.ScheduleDao;
import database.model.AppointmentEntity;
import database.model.MedicEntity;
import database.model.ScheduleEntity;
import database.model.SurgeryEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Calendar {

    private final ScheduleDao scheduleDao;
    private final AppointmentDao appointmentDao;

    private MedicEntity medic = new MedicEntity();
    private List<ScheduleEntity> medicSchedules = new ArrayList<>();
    private SurgeryEntity surgery;
    private Long surgeryTime;

    public Calendar() {
        this.scheduleDao = new ScheduleDao();
        this.appointmentDao = new AppointmentDao();
    }

    public void setMedic(MedicEntity medic) {
        this.medic = medic;
        this.medicSchedules = scheduleDao.getMedicSchedules(medic.getId());
    }

    public void setSurgery(SurgeryEntity surgery) {
        this.surgery = surgery;
        this.surgeryTime = surgery.getTime().getLong(ChronoField.HOUR_OF_DAY);
    }

    public boolean isDayAvailable(LocalDate date) {
        if (surgeryTime == null) return false;

        List<ScheduleEntity> schedulesForDay = getSchedulesByDay(date);
        if (schedulesForDay.isEmpty()) return false;

        subtractAppointmentsFromSchedules(schedulesForDay, date);

        for (ScheduleEntity schedule : schedulesForDay) {
            LocalTime minimumHour = schedule.getEndHour().minusHours(surgeryTime);
            if (!schedule.getStartHour().isAfter(minimumHour)) return true;
        }
        return false;
    }

    public List<LocalTime> getFreeHours(LocalDate date) {
        List<ScheduleEntity> schedulesForDay = getSchedulesByDay(date);
        subtractAppointmentsFromSchedules(schedulesForDay, date);

        List<LocalTime> freeHours = new ArrayList<>();
        for (ScheduleEntity schedule : schedulesForDay) {
            LocalTime minimumHour = schedule.getEndHour().minusHours(surgeryTime);
            if (!schedule.getStartHour().isAfter(minimumHour)) {
                for (LocalTime hour = schedule.getStartHour();
                     !hour.isAfter(minimumHour);
                     hour = hour.plusHours(surgeryTime)) {
                    freeHours.add(hour);
                }
            }
        }
        return freeHours;
    }

    public List<ScheduleEntity> getSchedulesByDay(LocalDate date) {
        return medicSchedules.stream()
                .map(s -> {
                    ScheduleEntity copy = new ScheduleEntity();
                    copy.setStartHour(s.getStartHour());
                    copy.setEndHour(s.getEndHour());
                    copy.setDay(s.getDay());
                    return copy;
                })
                .filter(s -> s.getDay().toString().equals(date.getDayOfWeek().toString()))
                .collect(Collectors.toList());
    }

    public void subtractAppointmentsFromSchedules(List<ScheduleEntity> schedulesForDay, LocalDate date) {
        List<AppointmentEntity> appointments = appointmentDao.getByDate(date);

        for (AppointmentEntity appointment : appointments) {
            // RF-05: acessa duração via associação JPA (sem surgeryDao separado)
            Long duration = appointment.getSurgery().getTime().getLong(ChronoField.HOUR_OF_DAY);
            LocalTime appointmentHour = appointment.getHour();

            for (int i = 0; i < schedulesForDay.size(); i++) {
                LocalTime start = schedulesForDay.get(i).getStartHour();
                LocalTime end = schedulesForDay.get(i).getEndHour();

                if (appointmentHour.equals(start)) {
                    schedulesForDay.get(i).setStartHour(appointmentHour.plusHours(duration));
                } else if (appointmentHour.isAfter(start) && appointmentHour.isBefore(end)) {
                    ScheduleEntity left = new ScheduleEntity();
                    left.setStartHour(start);
                    left.setEndHour(appointmentHour);

                    ScheduleEntity right = new ScheduleEntity();
                    right.setStartHour(appointmentHour.plusHours(duration));
                    right.setEndHour(end);

                    schedulesForDay.remove(i);
                    schedulesForDay.add(left);
                    schedulesForDay.add(right);
                }
            }
        }
    }
}
