package com.smartride.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.Location;

/** A booking as the employee sees it. Only here is the boarding OTP exposed (and only until boarding). */
public record EmployeeBookingView(
        Long id,
        BookingStatus status,
        LocalDate tripDate,
        String shiftTime,
        Location pickupLocation,
        Location dropLocation,
        Cab cab,
        String otp,
        Integer dropSequence,
        boolean alertSent,
        Integer rating,
        String feedback,
        LocalDateTime createdAt) {

    public static EmployeeBookingView from(Booking b) {
        String otp = b.getStatus() == BookingStatus.ASSIGNED ? b.getOtp() : null;
        return new EmployeeBookingView(b.getId(), b.getStatus(), b.getTripDate(), b.getShiftTime(),
                b.getPickupLocation(), b.getDropLocation(), b.getCab(), otp, b.getDropSequence(),
                b.isAlertSent(), b.getRating(), b.getFeedback(), b.getCreatedAt());
    }
}
