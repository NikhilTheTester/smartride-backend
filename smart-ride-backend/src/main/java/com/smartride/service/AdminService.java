package com.smartride.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import jakarta.persistence.PersistenceException;
import org.springframework.stereotype.Service;

import com.smartride.dto.CabRequest;
import com.smartride.dto.UserRequest;
import com.smartride.exception.ApiException;
import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.CabStatus;
import com.smartride.entity.Location;
import com.smartride.entity.Role;
import com.smartride.entity.User;
import com.smartride.dao.BookingDao;
import com.smartride.dao.CabDao;
import com.smartride.dao.LocationDao;
import com.smartride.dao.SosAlertDao;
import com.smartride.dao.UserDao;
import com.smartride.util.PasswordUtil;

/** Admin operations: manage employees, drivers, cabs, locations and view dashboard statistics. */
@Service
public class AdminService {

    private final UserDao userDao;
    private final CabDao cabDao;
    private final BookingDao bookingDao;
    private final LocationDao locationDao;
    private final SosAlertDao sosDao;
    private final RouteOptimizerService optimizer;

    public AdminService(UserDao userDao, CabDao cabDao, BookingDao bookingDao,
                        LocationDao locationDao, SosAlertDao sosDao, RouteOptimizerService optimizer) {
        this.userDao = userDao;
        this.cabDao = cabDao;
        this.bookingDao = bookingDao;
        this.locationDao = locationDao;
        this.sosDao = sosDao;
        this.optimizer = optimizer;
    }

    // ---------------- users (employees & drivers) ----------------

    public List<User> listUsers(Role role) {
        return role == null ? userDao.findAll() : userDao.findByRole(role);
    }

    public User createUser(UserRequest req) {
        if (blank(req.name()) || blank(req.email()) || blank(req.password())) {
            throw ApiException.badRequest("Name, email and password are required");
        }
        if (req.role() == null || req.role() == Role.ADMIN) {
            throw ApiException.badRequest("Role must be EMPLOYEE or DRIVER");
        }
        if (userDao.existsByEmail(req.email().trim())) {
            throw ApiException.badRequest("This email is already registered");
        }
        User u = new User();
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(PasswordUtil.hash(req.password()));
        u.setRole(req.role());
        copyUserFields(u, req);
        return userDao.save(u);
    }

    public User updateUser(Long id, UserRequest req) {
        User u = userDao.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        if (u.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("The admin account cannot be edited here");
        }
        if (blank(req.name())) {
            throw ApiException.badRequest("Name is required");
        }
        if (!blank(req.password())) {
            u.setPasswordHash(PasswordUtil.hash(req.password()));
        }
        copyUserFields(u, req);
        return userDao.save(u);
    }

    public void deleteUser(Long id) {
        User u = userDao.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        if (u.getRole() == Role.ADMIN) {
            throw ApiException.badRequest("The admin account cannot be deleted");
        }
        cabDao.findFirstByDriver(u).ifPresent(c -> {
            c.setDriver(null);
            cabDao.save(c);
        });
        try {
            userDao.delete(u);
        } catch (DataAccessException | PersistenceException e) {
            throw ApiException.badRequest("This user has trip history and cannot be deleted");
        }
    }

    private void copyUserFields(User u, UserRequest req) {
        u.setName(req.name().trim());
        u.setPhone(req.phone());
        u.setEmployeeCode(req.employeeCode());
        u.setBranch(req.branch());
        u.setLicenseNumber(req.licenseNumber());
    }

    // ---------------- cabs ----------------

    public List<Cab> listCabs() {
        return cabDao.findAll();
    }

    public Cab createCab(CabRequest req) {
        if (blank(req.cabNumber())) {
            throw ApiException.badRequest("Cab number is required");
        }
        if (cabDao.existsByCabNumber(req.cabNumber().trim())) {
            throw ApiException.badRequest("A cab with this number already exists");
        }
        Cab c = new Cab();
        applyCab(c, req, null);
        return cabDao.save(c);
    }

    public Cab updateCab(Long id, CabRequest req) {
        Cab c = cabDao.findById(id).orElseThrow(() -> ApiException.notFound("Cab not found"));
        applyCab(c, req, c);
        return cabDao.save(c);
    }

    public void deleteCab(Long id) {
        Cab c = cabDao.findById(id).orElseThrow(() -> ApiException.notFound("Cab not found"));
        try {
            cabDao.delete(c);
        } catch (DataAccessException | PersistenceException e) {
            throw ApiException.badRequest("This cab has bookings and cannot be deleted");
        }
    }

    private void applyCab(Cab c, CabRequest req, Cab existing) {
        if (req.capacity() == null || req.capacity() < 1) {
            throw ApiException.badRequest("Capacity must be at least 1");
        }
        c.setCabNumber(req.cabNumber().trim());
        c.setModel(req.model());
        c.setCapacity(req.capacity());
        if (req.driverId() == null) {
            c.setDriver(null);
            return;
        }
        User driver = userDao.findById(req.driverId()).orElseThrow(() -> ApiException.notFound("Driver not found"));
        if (driver.getRole() != Role.DRIVER) {
            throw ApiException.badRequest("Selected user is not a driver");
        }
        cabDao.findFirstByDriver(driver).ifPresent(other -> {
            if (existing == null || !other.getId().equals(existing.getId())) {
                throw ApiException.badRequest("This driver is already assigned to cab " + other.getCabNumber());
            }
        });
        c.setDriver(driver);
    }

    public List<Booking> optimizeRoute(Long cabId) {
        Cab cab = cabDao.findById(cabId).orElseThrow(() -> ApiException.notFound("Cab not found"));
        return optimizer.optimize(cab, LocalDate.now());
    }

    // ---------------- locations ----------------

    public Location addLocation(Location loc) {
        if (blank(loc.getName())) {
            throw ApiException.badRequest("Location name is required");
        }
        if (locationDao.existsByName(loc.getName().trim())) {
            throw ApiException.badRequest("This location already exists");
        }
        loc.setId(null);
        loc.setName(loc.getName().trim());
        return locationDao.save(loc);
    }

    // ---------------- dashboard statistics / reports ----------------

    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employees", userDao.countByRole(Role.EMPLOYEE));
        m.put("drivers", userDao.countByRole(Role.DRIVER));
        m.put("cabs", cabDao.count());
        m.put("cabsOnTrip", cabDao.countByStatus(CabStatus.ON_TRIP));
        m.put("bookingsToday", bookingDao.countByTripDate(LocalDate.now()));
        for (BookingStatus s : BookingStatus.values()) {
            m.put(s.name(), bookingDao.countByStatus(s));
        }
        m.put("openSos", sosDao.countUnresolved());
        return m;
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
