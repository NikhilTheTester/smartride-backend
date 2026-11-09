package com.smartride.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smartride.entity.SosAlert;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Data Access Object for the sos_alert table. */
@Repository
public class SosAlertDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public SosAlert save(SosAlert alert) {
        if (alert.getId() == null) {
            em.persist(alert);
            return alert;
        }
        return em.merge(alert);
    }

    public Optional<SosAlert> findById(Long id) {
        return Optional.ofNullable(em.find(SosAlert.class, id));
    }

    public List<SosAlert> findAll() {
        return em.createQuery("select s from SosAlert s order by s.id desc", SosAlert.class).getResultList();
    }

    public long countUnresolved() {
        return em.createQuery("select count(s) from SosAlert s where s.resolved = false", Long.class).getSingleResult();
    }
}
