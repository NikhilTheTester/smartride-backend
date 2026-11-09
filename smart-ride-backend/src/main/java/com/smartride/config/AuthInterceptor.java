package com.smartride.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.smartride.entity.Role;
import com.smartride.entity.User;
import com.smartride.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security gate for every /api/** request.
 * 1. /api/auth/** is public (login, register).
 * 2. Every other URL needs the header "Authorization: Bearer &lt;token&gt;".
 * 3. The URL prefix decides which role may enter: /api/admin -> ADMIN, /api/driver -> DRIVER, /api/employee -> EMPLOYEE.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // CORS pre-flight
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;
        User user = authService.findUserByToken(token);
        if (user == null) {
            return reject(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in again");
        }

        if (path.startsWith("/api/admin") && user.getRole() != Role.ADMIN) {
            return reject(response, HttpServletResponse.SC_FORBIDDEN, "Admin access only");
        }
        if (path.startsWith("/api/driver") && user.getRole() != Role.DRIVER) {
            return reject(response, HttpServletResponse.SC_FORBIDDEN, "Driver access only");
        }
        if (path.startsWith("/api/employee") && user.getRole() != Role.EMPLOYEE) {
            return reject(response, HttpServletResponse.SC_FORBIDDEN, "Employee access only");
        }

        request.setAttribute("currentUser", user);
        return true;
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }
}
