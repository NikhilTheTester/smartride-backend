package com.smartride.dto;


/** Employee feedback for a completed ride. */
public record RatingRequest(Integer rating, String feedback) {
}
