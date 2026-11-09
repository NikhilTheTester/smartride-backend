package com.smartride.dto;

import java.time.LocalDate;

/** Employee form to request a cab. */
public record BookingRequest(Long pickupLocationId, Long dropLocationId, LocalDate tripDate, String shiftTime) {
}
