package com.smartride.dto;


/** Admin form to create/update a cab. */
public record CabRequest(String cabNumber, String model, Integer capacity, Long driverId) {
}
