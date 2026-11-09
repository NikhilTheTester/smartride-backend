package com.smartride.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.smartride.dto.LoginRequest;
import com.smartride.dto.LoginResponse;
import com.smartride.dto.RegisterRequest;
import com.smartride.exception.ApiException;
import com.smartride.entity.Role;
import com.smartride.entity.User;
import com.smartride.dao.UserDao;
import com.smartride.util.PasswordUtil;

/** Login, employee registration and token handling. Tokens are kept in memory (restart = log in again). */
@Service
public class AuthService {

    private final UserDao userDao;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.password() == null) {
            throw ApiException.badRequest("Email and password are required");
        }
        User user = userDao.findByEmail(req.email().trim())
                .orElseThrow(() -> ApiException.badRequest("Invalid email or password"));
        if (!PasswordUtil.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.badRequest("Invalid email or password");
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, user.getId());
        return new LoginResponse(token, user);
    }

    public User register(RegisterRequest req) {
        if (isBlank(req.name()) || isBlank(req.email()) || isBlank(req.password())) {
            throw ApiException.badRequest("Name, email and password are required");
        }
        if (req.password().length() < 4) {
            throw ApiException.badRequest("Password must have at least 4 characters");
        }
        if (userDao.existsByEmail(req.email().trim())) {
            throw ApiException.badRequest("This email is already registered");
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(PasswordUtil.hash(req.password()));
        u.setPhone(req.phone());
        u.setEmployeeCode(req.employeeCode());
        u.setBranch(req.branch());
        u.setRole(Role.EMPLOYEE);
        return userDao.save(u);
    }

    /** Returns the user that owns this token, or null if the token is unknown. */
    public User findUserByToken(String token) {
        if (token == null) {
            return null;
        }
        Long id = tokens.get(token);
        return id == null ? null : userDao.findById(id).orElse(null);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
