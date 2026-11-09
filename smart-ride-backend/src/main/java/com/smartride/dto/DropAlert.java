package com.smartride.dto;


/** Triggered when the cab enters the geofence of an employee's drop point. */
public record DropAlert(Long bookingId, String employeeName, String dropLocationName, String message) {
}
