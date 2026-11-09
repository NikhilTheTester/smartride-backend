package com.smartride.resource;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.UserRequest;
import com.smartride.entity.Role;
import com.smartride.entity.User;
import com.smartride.service.AdminService;

/** ALL requests about managing employees and drivers (admin only). */
@RestController
@RequestMapping("/api/admin/users")
public class UserResource {

    private final AdminService adminService;

    public UserResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<User> list(@RequestParam(name = "role", required = false) Role role) {
        return adminService.listUsers(role);
    }

    @PostMapping
    public User create(@RequestBody UserRequest req) {
        return adminService.createUser(req);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable("id") Long id, @RequestBody UserRequest req) {
        return adminService.updateUser(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return Map.of("message", "User deleted");
    }
}
