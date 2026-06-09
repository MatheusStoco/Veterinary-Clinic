package database.dao;

import database.model.MedicEntity;

public class MedicDao extends AbstractDao<MedicEntity> {

    public MedicDao() {
        super(MedicEntity.class);
    }
}
