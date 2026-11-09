package com.smartride.resource;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartride.dto.AssignRequest;
import com.smartride.dto.BookingRequest;
import com.smartride.dto.EmployeeBookingView;
import com.smartride.dto.OtpRequest;
import com.smartride.dto.RatingRequest;
import com.smartride.entity.Booking;
import com.smartride.entity.User;
import com.smartride.service.BookingService;
import com.smartride.service.DriverService;

/**
 * ALL requests about a cab booking (ticket) are handled in this one class:
 * the employee books / cancels / rates, the admin lists and assigns, the driver verifies OTP / drops / marks no-show.
 */
@RestController
@RequestMapping("/api")
public class BookingResource {

    private final BookingService bookingService;
    private final DriverService driverService;

    public BookingResource(BookingService bookingService, DriverService driverService) {
        this.bookingService = bookingService;
        this.driverService = driverService;
    }

    // ---------------- employee ----------------

    @PostMapping("/employee/bookings")
    public EmployeeBookingView book(@RequestAttribute("currentUser") User user, @RequestBody BookingRequest req) {
        return EmployeeBookingView.from(bookingService.create(user, req));
    }

    @GetMapping("/employee/bookings")
    public List<EmployeeBookingView> myBookings(@RequestAttribute("currentUser") User user) {
        return bookingService.myBookings(user).stream().map(EmployeeBookingView::from).toList();
    }

    @PutMapping("/employee/bookings/{id}/cancel")
    public EmployeeBookingView cancel(@RequestAttribute("currentUser") User user, @PathVariable("id") Long id) {
        return EmployeeBookingView.from(bookingService.cancel(user, id));
    }

    @PostMapping("/employee/bookings/{id}/rate")
    public EmployeeBookingView rate(@RequestAttribute("currentUser") User user, @PathVariable("id") Long id,
                                    @RequestBody RatingRequest req) {
        return EmployeeBookingView.from(bookingService.rate(user, id, req));
    }

    // ---------------- admin ----------------

    @GetMapping("/admin/bookings")
    public List<Booking> allBookings() {
        return bookingService.allBookings();
    }

    @PutMapping("/admin/bookings/{id}/assign")
    public Booking assign(@PathVariable("id") Long id, @RequestBody AssignRequest req) {
        return bookingService.assign(id, req.cabId());
    }

    // ---------------- driver ----------------

    @PostMapping("/driver/bookings/{id}/verify-otp")
    public Booking verifyOtp(@RequestAttribute("currentUser") User driver, @PathVariable("id") Long id,
                             @RequestBody OtpRequest req) {
        return driverService.verifyOtp(driver, id, req.otp());
    }

    @PostMapping("/driver/bookings/{id}/drop")
    public Booking drop(@RequestAttribute("currentUser") User driver, @PathVariable("id") Long id) {
        return driverService.drop(driver, id);
    }

    @PostMapping("/driver/bookings/{id}/no-show")
    public Booking noShow(@RequestAttribute("currentUser") User driver, @PathVariable("id") Long id) {
        return driverService.noShow(driver, id);
    }
}
