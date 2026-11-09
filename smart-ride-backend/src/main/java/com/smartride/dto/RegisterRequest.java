package com.smartride.dto;


/** Body of POST /api/auth/register (employee self sign-up). */
public record RegisterRequest(String name, String email, String password, String phone, String employeeCode, String branch) {
}
