package database.dao;

import database.model.SurgeryEntity;

public class SurgeryDao extends AbstractDao<SurgeryEntity> {

    public SurgeryDao() {
        super(SurgeryEntity.class);
    }
}
