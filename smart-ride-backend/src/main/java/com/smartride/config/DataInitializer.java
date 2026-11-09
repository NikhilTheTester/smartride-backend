package com.smartride.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartride.entity.Booking;
import com.smartride.entity.BookingStatus;
import com.smartride.entity.Cab;
import com.smartride.entity.Location;
import com.smartride.entity.Role;
import com.smartride.entity.User;
import com.smartride.dao.BookingDao;
import com.smartride.dao.CabDao;
import com.smartride.dao.LocationDao;
import com.smartride.dao.UserDao;
import com.smartride.util.PasswordUtil;

/** Inserts demo data the first time the app starts (only when the users table is empty). */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final LocationDao locationDao;
    private final CabDao cabDao;
    private final BookingDao bookingDao;

    public DataInitializer(UserDao userDao, LocationDao locationDao, CabDao cabDao,
                           BookingDao bookingDao) {
        this.userDao = userDao;
        this.locationDao = locationDao;
        this.cabDao = cabDao;
        this.bookingDao = bookingDao;
    }

    @Override
    public void run(String... args) {
        // ---- locations (Hyderabad): inserted only if the table is empty ----
        Map<String, Location> loc = new HashMap<>();
        if (locationDao.count() == 0) {
            seedLocations(loc);
        }
        for (Location l : locationDao.findAllOrderByName()) {
            loc.put(l.getName(), l);
        }

        // ---- users, cabs and bookings: inserted only on a fresh database ----
        if (userDao.count() > 0) {
            return;
        }

        // ---- users ----
        addUser("Smart Ride Admin", "admin@smartride.com", "admin123", Role.ADMIN, "9000000000", null, null, null);
        User d1 = addUser("Ravi Kumar", "driver1@smartride.com", "driver123", Role.DRIVER, "9000000101", null, null, "TS-DL-1001");
        User d2 = addUser("Suresh Babu", "driver2@smartride.com", "driver123", Role.DRIVER, "9000000102", null, null, "TS-DL-1002");
        User d3 = addUser("Mahesh Goud", "driver3@smartride.com", "driver123", Role.DRIVER, "9000000103", null, null, "TS-DL-1003");

        User e1 = addUser("Arjun Rao", "emp1@smartride.com", "emp123", Role.EMPLOYEE, "9000000201", "EMP001", "Hitech City", null);
        User e2 = addUser("Priya Sharma", "emp2@smartride.com", "emp123", Role.EMPLOYEE, "9000000202", "EMP002", "Gachibowli", null);
        User e3 = addUser("Kiran Kumar", "emp3@smartride.com", "emp123", Role.EMPLOYEE, "9000000203", "EMP003", "Kondapur", null);
        User e4 = addUser("Sneha Reddy", "emp4@smartride.com", "emp123", Role.EMPLOYEE, "9000000204", "EMP004", "Financial District", null);
        addUser("Rahul Verma", "emp5@smartride.com", "emp123", Role.EMPLOYEE, "9000000205", "EMP005", "Madhapur", null);

        // ---- cabs (each starts at a different place so the live map shows them) ----
        addCab("TS09 AB 1234", "Maruti Swift Dzire", 4, d1, loc.get("Ameerpet"));
        addCab("TS09 CD 5678", "Toyota Innova", 6, d2, loc.get("Kukatpally"));
        addCab("TS10 EF 9012", "Tempo Traveller", 12, d3, loc.get("Secunderabad"));

        // ---- some requested rides for today so the admin can assign them straight away ----
        addBooking(e1, loc.get("Ameerpet"), loc.get("Madhapur"), "09:00 AM");
        addBooking(e2, loc.get("Ameerpet"), loc.get("Gachibowli"), "09:00 AM");
        addBooking(e3, loc.get("Ameerpet"), loc.get("Kondapur"), "09:00 AM");
        addBooking(e4, loc.get("Ameerpet"), loc.get("Financial District"), "09:00 AM");

        System.out.println("Smart Ride demo data created. Admin login: admin@smartride.com / admin123");
    }

    private void seedLocations(Map<String, Location> loc) {
        addLoc(loc, "Ameerpet", 17.4375, 78.4482);
        addLoc(loc, "Kukatpally", 17.4948, 78.3996);
        addLoc(loc, "Miyapur", 17.4967, 78.3563);
        addLoc(loc, "Madhapur", 17.4486, 78.3908);
        addLoc(loc, "Hitech City", 17.4435, 78.3772);
        addLoc(loc, "Kondapur", 17.4616, 78.3642);
        addLoc(loc, "Gachibowli", 17.4401, 78.3489);
        addLoc(loc, "Financial District", 17.4172, 78.3430);
        addLoc(loc, "Jubilee Hills", 17.4326, 78.4071);
        addLoc(loc, "Banjara Hills", 17.4156, 78.4347);
        addLoc(loc, "Secunderabad", 17.4399, 78.4983);
        addLoc(loc, "Uppal", 17.4058, 78.5591);
    }

    private void addLoc(Map<String, Location> map, String name, double lat, double lng) {
        Location l = new Location();
        l.setName(name);
        l.setLatitude(lat);
        l.setLongitude(lng);
        map.put(name, locationDao.save(l));
    }

    private User addUser(String name, String email, String password, Role role, String phone, String code,
                         String branch, String license) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setRole(role);
        u.setPhone(phone);
        u.setEmployeeCode(code);
        u.setBranch(branch);
        u.setLicenseNumber(license);
        return userDao.save(u);
    }

    private void addCab(String number, String model, int capacity, User driver, Location start) {
        Cab c = new Cab();
        c.setCabNumber(number);
        c.setModel(model);
        c.setCapacity(capacity);
        c.setDriver(driver);
        c.setCurrentLat(start.getLatitude());
        c.setCurrentLng(start.getLongitude());
        c.setLastUpdated(LocalDateTime.now());
        cabDao.save(c);
    }

    private void addBooking(User employee, Location pickup, Location drop, String shift) {
        Booking b = new Booking();
        b.setEmployee(employee);
        b.setPickupLocation(pickup);
        b.setDropLocation(drop);
        b.setTripDate(LocalDate.now());
        b.setShiftTime(shift);
        b.setStatus(BookingStatus.REQUESTED);
        bookingDao.save(b);
    }
}
