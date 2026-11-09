package com.smartride.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Data Access Object for the booking table (cab requests / trips). */
@Repository
public class BookingDao {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            em.persist(booking);
            return booking;
        }
        return em.merge(booking);
    }

    @Transactional
    public List<Booking> saveAll(List<Booking> bookings) {
        List<Booking> saved = new ArrayList<>();
        for (Booking b : bookings) {
            saved.add(save(b));
        }
        return saved;
    }

    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(em.find(Booking.class, id));
    }

    public List<Booking> findAll() {
        return em.createQuery("select b from Booking b order by b.id desc", Booking.class).getResultList();
    }

    public List<Booking> findByEmployee(User employee) {
        return em.createQuery("select b from Booking b where b.employee = :e order by b.id desc", Booking.class)
                .setParameter("e", employee)
                .getResultList();
    }

    public boolean existsForEmployee(User employee, LocalDate date, Collection<BookingStatus> statuses) {
        Long n = em.createQuery("select count(b) from Booking b where b.employee = :e and b.tripDate = :d and b.status in :s", Long.class)
                .setParameter("e", employee)
                .setParameter("d", date)
                .setParameter("s", statuses)
                .getSingleResult();
        return n > 0;
    }

    public long countForCab(Cab cab, LocalDate date, Collection<BookingStatus> statuses) {
        return em.createQuery("select count(b) from Booking b where b.cab = :c and b.tripDate = :d and b.status in :s", Long.class)
                .setParameter("c", cab)
                .setParameter("d", date)
                .setParameter("s", statuses)
                .getSingleResult();
    }

    public List<Booking> findForCab(Cab cab, LocalDate date, Collection<BookingStatus> statuses) {
        return em.createQuery("select b from Booking b where b.cab = :c and b.tripDate = :d and b.status in :s order by b.id asc", Booking.class)
                .setParameter("c", cab)
                .setParameter("d", date)
                .setParameter("s", statuses)
                .getResultList();
    }

    public List<Booking> findAllForCab(Cab cab, LocalDate date) {
        return em.createQuery("select b from Booking b where b.cab = :c and b.tripDate = :d order by b.dropSequence asc, b.id asc", Booking.class)
                .setParameter("c", cab)
                .setParameter("d", date)
                .getResultList();
    }

    public long countByStatus(BookingStatus status) {
        return em.createQuery("select count(b) from Booking b where b.status = :s", Long.class)
                .setParameter("s", status)
                .getSingleResult();
    }

    public long countByTripDate(LocalDate date) {
        return em.createQuery("select count(b) from Booking b where b.tripDate = :d", Long.class)
                .setParameter("d", date)
                .getSingleResult();
    }
}
