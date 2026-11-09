package com.smartride.dto;

import com.smartride.entity.User;

/** Returned after a successful login. */
public record LoginResponse(String token, User user) {
}
