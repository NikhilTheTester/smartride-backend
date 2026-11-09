package com.smartride.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.smartride.dto.BookingRequest;
import com.smartride.dto.RatingRequest;
import com.smartride.exception.ApiException;
import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.Location;
import com.smartride.entity.User;
import com.smartride.dao.BookingDao;
import com.smartride.dao.CabDao;
import com.smartride.dao.LocationDao;

/** Employee booking requests and the admin's cab assignment (with capacity check and OTP generation). */
@Service
public class BookingService {

    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.REQUESTED, BookingStatus.ASSIGNED, BookingStatus.BOARDED);
    private static final List<BookingStatus> SEATED = List.of(BookingStatus.ASSIGNED, BookingStatus.BOARDED);

    private final BookingDao bookingDao;
    private final LocationDao locationDao;
    private final CabDao cabDao;
    private final RouteOptimizerService optimizer;
    private final Random random = new Random();

    public BookingService(BookingDao bookingDao, LocationDao locationDao, CabDao cabDao,
                          RouteOptimizerService optimizer) {
        this.bookingDao = bookingDao;
        this.locationDao = locationDao;
        this.cabDao = cabDao;
        this.optimizer = optimizer;
    }

    // ---------------- employee side ----------------

    public Booking create(User employee, BookingRequest req) {
        if (req.pickupLocationId() == null || req.dropLocationId() == null) {
            throw ApiException.badRequest("Please select pickup and drop locations");
        }
        if (req.pickupLocationId().equals(req.dropLocationId())) {
            throw ApiException.badRequest("Pickup and drop locations cannot be the same");
        }
        Location pickup = locationDao.findById(req.pickupLocationId())
                .orElseThrow(() -> ApiException.notFound("Pickup location not found"));
        Location drop = locationDao.findById(req.dropLocationId())
                .orElseThrow(() -> ApiException.notFound("Drop location not found"));
        LocalDate date = req.tripDate() == null ? LocalDate.now() : req.tripDate();
        if (date.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("Trip date cannot be in the past");
        }
        if (bookingDao.existsForEmployee(employee, date, ACTIVE)) {
            throw ApiException.badRequest("You already have an active booking for " + date);
        }
        Booking b = new Booking();
        b.setEmployee(employee);
        b.setPickupLocation(pickup);
        b.setDropLocation(drop);
        b.setTripDate(date);
        b.setShiftTime(req.shiftTime());
        b.setStatus(BookingStatus.REQUESTED);
        return bookingDao.save(b);
    }

    public List<Booking> myBookings(User employee) {
        return bookingDao.findByEmployee(employee);
    }

    public Booking cancel(User employee, Long id) {
        Booking b = ownedBooking(employee, id);
        if (b.getStatus() != BookingStatus.REQUESTED && b.getStatus() != BookingStatus.ASSIGNED) {
            throw ApiException.badRequest("This booking can no longer be cancelled");
        }
        Cab cab = b.getCab();
        b.setStatus(BookingStatus.CANCELLED);
        bookingDao.save(b);
        if (cab != null) {
            optimizer.optimize(cab, b.getTripDate());
        }
        return b;
    }

    public Booking rate(User employee, Long id, RatingRequest req) {
        Booking b = ownedBooking(employee, id);
        if (b.getStatus() != BookingStatus.DROPPED) {
            throw ApiException.badRequest("You can rate only completed rides");
        }
        if (req.rating() == null || req.rating() < 1 || req.rating() > 5) {
            throw ApiException.badRequest("Rating must be between 1 and 5");
        }
        b.setRating(req.rating());
        b.setFeedback(req.feedback());
        return bookingDao.save(b);
    }

    // ---------------- admin side ----------------

    public List<Booking> allBookings() {
        return bookingDao.findAll();
    }

    public Booking assign(Long bookingId, Long cabId) {
        Booking b = bookingDao.findById(bookingId).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (b.getStatus() != BookingStatus.REQUESTED && b.getStatus() != BookingStatus.ASSIGNED) {
            throw ApiException.badRequest("Only REQUESTED or ASSIGNED bookings can be assigned");
        }
        if (cabId == null) {
            throw ApiException.badRequest("Please select a cab");
        }
        Cab cab = cabDao.findById(cabId).orElseThrow(() -> ApiException.notFound("Cab not found"));
        if (cab.getDriver() == null) {
            throw ApiException.badRequest("This cab has no driver. Assign a driver to the cab first");
        }
        boolean sameCab = b.getCab() != null && b.getCab().getId().equals(cabId);
        if (!sameCab) {
            long used = bookingDao.countForCab(cab, b.getTripDate(), SEATED);
            if (used >= cab.getCapacity()) {
                throw ApiException.badRequest("Cab " + cab.getCabNumber() + " is full (" + cab.getCapacity() + " seats)");
            }
        }
        Cab oldCab = b.getCab();
        b.setCab(cab);
        b.setStatus(BookingStatus.ASSIGNED);
        b.setAlertSent(false);
        if (b.getOtp() == null) {
            b.setOtp(String.format("%04d", random.nextInt(10000)));
        }
        bookingDao.save(b);
        optimizer.optimize(cab, b.getTripDate());
        if (oldCab != null && !sameCab) {
            optimizer.optimize(oldCab, b.getTripDate());
        }
        return bookingDao.findById(b.getId()).orElse(b);
    }

    private Booking ownedBooking(User employee, Long id) {
        Booking b = bookingDao.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!b.getEmployee().getId().equals(employee.getId())) {
            throw ApiException.notFound("Booking not found");
        }
        return b;
    }
}
