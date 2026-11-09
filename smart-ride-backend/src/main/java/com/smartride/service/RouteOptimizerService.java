package com.smartride.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.Location;
import com.smartride.dao.BookingDao;
import com.smartride.util.GeoUtil;

/**
 * Smart Route Optimization: decides the order in which a cab should drop its passengers.
 * Algorithm: nearest-neighbour. Start from the cab's position, repeatedly go to the closest remaining drop point.
 */
@Service
public class RouteOptimizerService {

    private final BookingDao bookingDao;

    public RouteOptimizerService(BookingDao bookingDao) {
        this.bookingDao = bookingDao;
    }

    public List<Booking> optimize(Cab cab, LocalDate date) {
        List<Booking> remaining = new ArrayList<>(bookingDao.findForCab(
                cab, date, List.of(BookingStatus.ASSIGNED, BookingStatus.BOARDED)));
        List<Booking> ordered = new ArrayList<>();
        if (remaining.isEmpty()) {
            return ordered;
        }

        double curLat;
        double curLng;
        if (cab.getCurrentLat() != null && cab.getCurrentLng() != null) {
            curLat = cab.getCurrentLat();
            curLng = cab.getCurrentLng();
        } else {
            Location first = remaining.get(0).getPickupLocation();
            curLat = first.getLatitude();
            curLng = first.getLongitude();
        }

        int sequence = 1;
        while (!remaining.isEmpty()) {
            Booking best = null;
            double bestDistance = Double.MAX_VALUE;
            for (Booking candidate : remaining) {
                Location drop = candidate.getDropLocation();
                double d = GeoUtil.distanceMeters(curLat, curLng, drop.getLatitude(), drop.getLongitude());
                if (d < bestDistance) {
                    bestDistance = d;
                    best = candidate;
                }
            }
            best.setDropSequence(sequence++);
            ordered.add(best);
            remaining.remove(best);
            curLat = best.getDropLocation().getLatitude();
            curLng = best.getDropLocation().getLongitude();
        }
        bookingDao.saveAll(ordered);
        return ordered;
    }
}
