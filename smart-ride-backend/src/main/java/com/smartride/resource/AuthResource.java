package com.smartride.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.LoginRequest;
import com.smartride.dto.LoginResponse;
import com.smartride.dto.RegisterRequest;
import com.smartride.entity.User;
import com.smartride.service.AuthService;

/**
 * Everything about authentication that comes from the frontend:
 * login, employee registration and "who am I".
 */
@RestController
@RequestMapping("/api")
public class AuthResource {

    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/auth/register")
    public User register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @GetMapping("/common/me")
    public User me(@RequestAttribute("currentUser") User user) {
        return user;
    }
}
