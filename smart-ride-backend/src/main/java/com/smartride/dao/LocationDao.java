package com.smartride.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smartride.entity.Location;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Data Access Object for the location table (pickup / drop places). */
@Repository
public class LocationDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Location save(Location location) {
        if (location.getId() == null) {
            em.persist(location);
            return location;
        }
        return em.merge(location);
    }

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(em.find(Location.class, id));
    }

    public List<Location> findAllOrderByName() {
        return em.createQuery("select l from Location l order by l.name asc", Location.class).getResultList();
    }

    public boolean existsByName(String name) {
        Long n = em.createQuery("select count(l) from Location l where lower(l.name) = lower(:name)", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return n > 0;
    }

    public long count() {
        return em.createQuery("select count(l) from Location l", Long.class).getSingleResult();
    }
}
