package com.smartride.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smartride.entity.Cab;
import com.smartride.entity.CabStatus;
import com.smartride.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Data Access Object for the cab table. */
@Repository
public class CabDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Cab save(Cab cab) {
        if (cab.getId() == null) {
            em.persist(cab);
            return cab;
        }
        return em.merge(cab);
    }

    @Transactional
    public void delete(Cab cab) {
        em.remove(em.contains(cab) ? cab : em.merge(cab));
        em.flush();
    }

    public Optional<Cab> findById(Long id) {
        return Optional.ofNullable(em.find(Cab.class, id));
    }

    public List<Cab> findAll() {
        return em.createQuery("select c from Cab c order by c.id asc", Cab.class).getResultList();
    }

    public Optional<Cab> findFirstByDriver(User driver) {
        return em.createQuery("select c from Cab c where c.driver = :driver", Cab.class)
                .setParameter("driver", driver)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByCabNumber(String cabNumber) {
        Long n = em.createQuery("select count(c) from Cab c where lower(c.cabNumber) = lower(:no)", Long.class)
                .setParameter("no", cabNumber)
                .getSingleResult();
        return n > 0;
    }

    public long count() {
        return em.createQuery("select count(c) from Cab c", Long.class).getSingleResult();
    }

    public long countByStatus(CabStatus status) {
        return em.createQuery("select count(c) from Cab c where c.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }
}
