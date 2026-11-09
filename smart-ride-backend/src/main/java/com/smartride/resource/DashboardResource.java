package com.smartride.resource;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.service.AdminService;

/** Report numbers for the admin overview cards. */
@RestController
@RequestMapping("/api/admin")
public class DashboardResource {

    private final AdminService adminService;

    public DashboardResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return adminService.stats();
    }
}
