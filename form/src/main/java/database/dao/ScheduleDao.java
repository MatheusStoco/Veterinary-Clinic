package database.dao;

import database.model.ScheduleEntity;

import javax.persistence.TypedQuery;
import java.util.List;

public class ScheduleDao extends AbstractDao<ScheduleEntity> {

    public ScheduleDao() {
        super(ScheduleEntity.class);
    }

    public List<ScheduleEntity> getMedicSchedules(Long id) {
        TypedQuery<ScheduleEntity> query = connection.getEntityManager()
                .createQuery("SELECT s FROM ScheduleEntity s WHERE s.idMedic = :id", ScheduleEntity.class)
                .setParameter("id", id);
        return query.getResultList();
    }
}
