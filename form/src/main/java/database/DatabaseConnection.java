package database;

import database.exception.PersistenceException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.function.Consumer;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    private DatabaseConnection() {
        initTransaction();
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public boolean executeTransaction(Consumer<EntityManager> action) {
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            entityTransaction.begin();
            action.accept(entityManager);
            entityTransaction.commit();
        } catch (RuntimeException e) {
            entityTransaction.rollback();
            throw new PersistenceException("Erro na transação: " + e.getLocalizedMessage(), e);
        }
        return true;
    }

    private void initTransaction() {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("VeterinaryClinicPersistence");
            entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            throw new PersistenceException("Erro ao inicializar conexão com o banco: " + e.getMessage(), e);
        }
    }
}
