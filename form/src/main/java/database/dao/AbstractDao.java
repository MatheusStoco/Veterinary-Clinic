package database.dao;

import database.DatabaseConnection;

import javax.persistence.TypedQuery;
import java.util.List;

public abstract class AbstractDao<T> implements DaoI<T> {

    protected final DatabaseConnection connection = DatabaseConnection.getInstance();
    private final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T get(Long id) {
        return connection.getEntityManager().find(entityClass, id);
    }

    @Override
    public List<T> getAll() {
        String className = entityClass.getSimpleName();
        TypedQuery<T> query = connection.getEntityManager()
                .createQuery("SELECT e FROM " + className + " e", entityClass);
        return query.getResultList();
    }

    @Override
    public void create(T entity) {
        connection.executeTransaction(em -> em.persist(entity));
    }

    @Override
    public void update(T entity) {
        connection.executeTransaction(em -> em.merge(entity));
    }

    @Override
    public void delete(Long id) {
        connection.executeTransaction(em -> {
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
        });
    }
}
