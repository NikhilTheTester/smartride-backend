package com.smartride.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartride.dto.DriverTripView;
import com.smartride.dto.DropAlert;
import com.smartride.exception.ApiException;
import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.CabStatus;
import com.smartride.entity.Location;
import com.smartride.entity.User;
import com.smartride.dao.BookingDao;
import com.smartride.dao.CabDao;
import com.smartride.util.GeoUtil;

/** Everything a driver does: view trip, start/end, verify OTP, drop, no-show and send GPS (geofence alerts). */
@Service
public class DriverService {

    private final CabDao cabDao;
    private final BookingDao bookingDao;
    private final double radiusMeters;

    public DriverService(CabDao cabDao, BookingDao bookingDao,
                         @Value("${smartride.geofence.radius-meters:200}") double radiusMeters) {
        this.cabDao = cabDao;
        this.bookingDao = bookingDao;
        this.radiusMeters = radiusMeters;
    }

    public DriverTripView getTrip(User driver) {
        Cab cab = cabOf(driver);
        List<Booking> passengers = new ArrayList<>(bookingDao.findAllForCab(cab, LocalDate.now()));
        passengers.removeIf(b -> b.getStatus() == BookingStatus.CANCELLED);
        passengers.sort(Comparator.comparing(Booking::getDropSequence, Comparator.nullsLast(Comparator.naturalOrder())));
        return new DriverTripView(cab, passengers);
    }

    public Cab startTrip(User driver) {
        Cab cab = cabOf(driver);
        long seated = bookingDao.countForCab(cab, LocalDate.now(),
                List.of(BookingStatus.ASSIGNED, BookingStatus.BOARDED));
        if (seated == 0) {
            throw ApiException.badRequest("No passengers are assigned to your cab today");
        }
        cab.setStatus(CabStatus.ON_TRIP);
        return cabDao.save(cab);
    }

    public Cab endTrip(User driver) {
        Cab cab = cabOf(driver);
        List<Booking> today = bookingDao.findAllForCab(cab, LocalDate.now());
        for (Booking b : today) {
            if (b.getStatus() == BookingStatus.BOARDED) {
                throw ApiException.badRequest("Drop all boarded passengers before ending the trip");
            }
        }
        for (Booking b : today) {
            if (b.getStatus() == BookingStatus.ASSIGNED) {
                b.setStatus(BookingStatus.NO_SHOW);
                bookingDao.save(b);
            }
        }
        cab.setStatus(CabStatus.AVAILABLE);
        return cabDao.save(cab);
    }

    public Booking verifyOtp(User driver, Long bookingId, String otp) {
        Booking b = ownedPassenger(driver, bookingId);
        if (b.getStatus() != BookingStatus.ASSIGNED) {
            throw ApiException.badRequest("This passenger is not waiting to board");
        }
        if (otp == null || !otp.trim().equals(b.getOtp())) {
            throw ApiException.badRequest("Invalid OTP. Ask the employee for the OTP shown in their app");
        }
        b.setStatus(BookingStatus.BOARDED);
        b.setBoardedAt(LocalDateTime.now());
        return bookingDao.save(b);
    }

    public Booking drop(User driver, Long bookingId) {
        Booking b = ownedPassenger(driver, bookingId);
        if (b.getStatus() != BookingStatus.BOARDED) {
            throw ApiException.badRequest("Only boarded passengers can be dropped");
        }
        b.setStatus(BookingStatus.DROPPED);
        b.setDroppedAt(LocalDateTime.now());
        return bookingDao.save(b);
    }

    public Booking noShow(User driver, Long bookingId) {
        Booking b = ownedPassenger(driver, bookingId);
        if (b.getStatus() != BookingStatus.ASSIGNED) {
            throw ApiException.badRequest("Only passengers who have not boarded can be marked as no-show");
        }
        b.setStatus(BookingStatus.NO_SHOW);
        return bookingDao.save(b);
    }

    /**
     * Stores the cab's new GPS position and checks the geofence of every boarded passenger.
     * Each passenger triggers an alert only once (alertSent flag).
     */
    public List<DropAlert> updateLocation(User driver, Double lat, Double lng) {
        if (lat == null || lng == null) {
            throw ApiException.badRequest("Latitude and longitude are required");
        }
        Cab cab = cabOf(driver);
        cab.setCurrentLat(lat);
        cab.setCurrentLng(lng);
        cab.setLastUpdated(LocalDateTime.now());
        cabDao.save(cab);

        List<DropAlert> alerts = new ArrayList<>();
        List<Booking> boarded = bookingDao.findForCab(cab, LocalDate.now(), List.of(BookingStatus.BOARDED));
        for (Booking b : boarded) {
            if (b.isAlertSent()) {
                continue;
            }
            Location drop = b.getDropLocation();
            double distance = GeoUtil.distanceMeters(lat, lng, drop.getLatitude(), drop.getLongitude());
            if (distance <= radiusMeters) {
                b.setAlertSent(true);
                bookingDao.save(b);
                String message = "Attention! " + b.getEmployee().getName() + "'s destination " + drop.getName()
                        + " has arrived. Please drop the employee.";
                alerts.add(new DropAlert(b.getId(), b.getEmployee().getName(), drop.getName(), message));
            }
        }
        return alerts;
    }

    private Cab cabOf(User driver) {
        return cabDao.findFirstByDriver(driver)
                .orElseThrow(() -> ApiException.notFound("No cab is assigned to you yet. Please contact the admin"));
    }

    private Booking ownedPassenger(User driver, Long bookingId) {
        Cab cab = cabOf(driver);
        Booking b = bookingDao.findById(bookingId).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (b.getCab() == null || !b.getCab().getId().equals(cab.getId())) {
            throw ApiException.notFound("This passenger is not in your cab");
        }
        return b;
    }
}
