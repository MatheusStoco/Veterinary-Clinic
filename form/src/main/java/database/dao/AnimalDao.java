package database.dao;

import database.model.AnimalEntity;

import javax.persistence.TypedQuery;
import java.util.List;

public class AnimalDao extends AbstractDao<AnimalEntity> {

    public AnimalDao() {
        super(AnimalEntity.class);
    }

    public List<String> getAllSpecies() {
        TypedQuery<String> query = connection.getEntityManager()
                .createQuery("SELECT DISTINCT a.species FROM AnimalEntity a", String.class);
        return query.getResultList();
    }

    public List<AnimalEntity> getAllBySpecies(String species) {
        TypedQuery<AnimalEntity> query = connection.getEntityManager()
                .createQuery("SELECT a FROM AnimalEntity a WHERE a.species = :species", AnimalEntity.class)
                .setParameter("species", species);
        return query.getResultList();
    }
}
