package database.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointment", schema = "public", catalog = "veterinary_clinic")
public class AppointmentEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Basic
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Basic
    @Column(name = "hour", nullable = false)
    private LocalTime hour;

    @Basic
    @Column(name = "notes", length = -1)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "id_surgery", nullable = false)
    private SurgeryEntity surgery;

    @ManyToOne
    @JoinColumn(name = "id_medic", nullable = false)
    private MedicEntity medic;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private ClientEntity client;

    @ManyToOne
    @JoinColumn(name = "id_animal", nullable = false)
    private AnimalEntity animal;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHour() { return hour; }
    public void setHour(LocalTime hour) { this.hour = hour; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public SurgeryEntity getSurgery() { return surgery; }
    public void setSurgery(SurgeryEntity surgery) { this.surgery = surgery; }

    public MedicEntity getMedic() { return medic; }
    public void setMedic(MedicEntity medic) { this.medic = medic; }

    public ClientEntity getClient() { return client; }
    public void setClient(ClientEntity client) { this.client = client; }

    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity animal) { this.animal = animal; }

    @Override
    public String toString() {
        return "AppointmentEntity{date=" + date + ", hour=" + hour + ", notes='" + notes + "'}";
    }
}
