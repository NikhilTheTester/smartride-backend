package com.smartride.dto;


/** A GPS position sent by the driver device or by an SOS button. */
public record GpsPing(Double lat, Double lng) {
}
