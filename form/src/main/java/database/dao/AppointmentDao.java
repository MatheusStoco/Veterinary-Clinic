package database.dao;

import database.model.AppointmentEntity;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class AppointmentDao extends AbstractDao<AppointmentEntity> {

    public AppointmentDao() {
        super(AppointmentEntity.class);
    }

    public List<AppointmentEntity> getByDate(LocalDate date) {
        TypedQuery<AppointmentEntity> query = connection.getEntityManager()
                .createQuery("SELECT a FROM AppointmentEntity a WHERE a.date = :date ORDER BY a.hour ASC", AppointmentEntity.class)
                .setParameter("date", date);
        return query.getResultList();
    }
}
