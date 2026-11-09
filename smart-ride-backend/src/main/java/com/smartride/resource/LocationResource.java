package com.smartride.resource;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dao.LocationDao;
import com.smartride.entity.Location;
import com.smartride.service.AdminService;

/** ALL requests about pickup / drop locations. */
@RestController
@RequestMapping("/api")
public class LocationResource {

    private final LocationDao locationDao;
    private final AdminService adminService;

    public LocationResource(LocationDao locationDao, AdminService adminService) {
        this.locationDao = locationDao;
        this.adminService = adminService;
    }

    /** Any logged-in user (used by the booking form drop-downs). */
    @GetMapping("/common/locations")
    public List<Location> list() {
        return locationDao.findAllOrderByName();
    }

    /** Admin adds a new location. */
    @PostMapping("/admin/locations")
    public Location add(@RequestBody Location location) {
        return adminService.addLocation(location);
    }
}
