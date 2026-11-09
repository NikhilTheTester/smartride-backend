package com.smartride.resource;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.CabRequest;
import com.smartride.dto.DriverTripView;
import com.smartride.dto.DropAlert;
import com.smartride.dto.GpsPing;
import com.smartride.entity.Booking;
import com.smartride.entity.Cab;
import com.smartride.entity.User;
import com.smartride.service.AdminService;
import com.smartride.service.DriverService;

/**
 * ALL requests about cabs and the cab's trip are handled here:
 * admin cab management + route optimization, and the driver's trip (view, start, end, GPS location).
 */
@RestController
@RequestMapping("/api")
public class CabResource {

    private final AdminService adminService;
    private final DriverService driverService;

    public CabResource(AdminService adminService, DriverService driverService) {
        this.adminService = adminService;
        this.driverService = driverService;
    }

    // ---------------- admin: cab management ----------------

    @GetMapping("/admin/cabs")
    public List<Cab> cabs() {
        return adminService.listCabs();
    }

    @PostMapping("/admin/cabs")
    public Cab createCab(@RequestBody CabRequest req) {
        return adminService.createCab(req);
    }

    @PutMapping("/admin/cabs/{id}")
    public Cab updateCab(@PathVariable("id") Long id, @RequestBody CabRequest req) {
        return adminService.updateCab(id, req);
    }

    @DeleteMapping("/admin/cabs/{id}")
    public Map<String, String> deleteCab(@PathVariable("id") Long id) {
        adminService.deleteCab(id);
        return Map.of("message", "Cab deleted");
    }

    @PostMapping("/admin/cabs/{id}/optimize")
    public List<Booking> optimizeRoute(@PathVariable("id") Long id) {
        return adminService.optimizeRoute(id);
    }

    // ---------------- driver: trip ----------------

    @GetMapping("/driver/trip")
    public DriverTripView trip(@RequestAttribute("currentUser") User driver) {
        return driverService.getTrip(driver);
    }

    @PostMapping("/driver/trip/start")
    public Cab startTrip(@RequestAttribute("currentUser") User driver) {
        return driverService.startTrip(driver);
    }

    @PostMapping("/driver/trip/end")
    public Cab endTrip(@RequestAttribute("currentUser") User driver) {
        return driverService.endTrip(driver);
    }

    /** Called every few seconds with the cab's GPS position. Returns the voice alerts to play. */
    @PostMapping("/driver/location")
    public List<DropAlert> updateLocation(@RequestAttribute("currentUser") User driver, @RequestBody GpsPing ping) {
        return driverService.updateLocation(driver, ping.lat(), ping.lng());
    }
}
