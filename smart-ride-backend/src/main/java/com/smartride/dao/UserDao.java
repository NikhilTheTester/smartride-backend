package com.smartride.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smartride.entity.Role;
import com.smartride.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Data Access Object for the users table. All SQL/JPQL for users lives here. */
@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            em.persist(user);
            return user;
        }
        return em.merge(user);
    }

    @Transactional
    public void delete(User user) {
        em.remove(em.contains(user) ? user : em.merge(user));
        em.flush(); // forces foreign-key errors to appear here
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        return em.createQuery("select u from User u where lower(u.email) = lower(:email)", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByEmail(String email) {
        Long n = em.createQuery("select count(u) from User u where lower(u.email) = lower(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return n > 0;
    }

    public List<User> findAll() {
        return em.createQuery("select u from User u order by u.id desc", User.class).getResultList();
    }

    public List<User> findByRole(Role role) {
        return em.createQuery("select u from User u where u.role = :role order by u.id desc", User.class)
                .setParameter("role", role)
                .getResultList();
    }

    public long count() {
        return em.createQuery("select count(u) from User u", Long.class).getSingleResult();
    }

    public long countByRole(Role role) {
        return em.createQuery("select count(u) from User u where u.role = :role", Long.class)
                .setParameter("role", role)
                .getSingleResult();
    }
}
