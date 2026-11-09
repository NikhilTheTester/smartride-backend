package com.smartride.dto;

import com.smartride.entity.Role;

/** Admin form to create/update an employee or driver. */
public record UserRequest(String name, String email, String password, String phone, Role role, String employeeCode, String branch, String licenseNumber) {
}
