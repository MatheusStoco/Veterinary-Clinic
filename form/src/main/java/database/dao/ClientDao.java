package database.dao;

import database.model.ClientEntity;

public class ClientDao extends AbstractDao<ClientEntity> {

    public ClientDao() {
        super(ClientEntity.class);
    }
}
