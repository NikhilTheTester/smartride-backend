package com.smartride.dto;

import java.util.List;

import com.smartride.entity.Booking;
import com.smartride.entity.Cab;

/** Everything the driver dashboard needs: the cab and today's passengers in drop order. */
public record DriverTripView(Cab cab, List<Booking> passengers) {
}
