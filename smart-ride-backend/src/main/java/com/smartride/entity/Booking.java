package com.smartride.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** An employee's request for a cab ride on a date, from a pickup point to a drop point. */
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_location_id")
    private Location pickupLocation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "drop_location_id")
    private Location dropLocation;

    private LocalDate tripDate;
    private String shiftTime;

    @ManyToOne
    private Cab cab;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.REQUESTED;

    /** Boarding OTP. Hidden from JSON so the driver can never see it; only the employee view exposes it. */
    @JsonIgnore
    private String otp;

    private Integer dropSequence;
    private boolean alertSent;
    private LocalDateTime boardedAt;
    private LocalDateTime droppedAt;
    private Integer rating;
    private String feedback;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(Location pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(Location dropLocation) {
        this.dropLocation = dropLocation;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getShiftTime() {
        return shiftTime;
    }

    public void setShiftTime(String shiftTime) {
        this.shiftTime = shiftTime;
    }

    public Cab getCab() {
        return cab;
    }

    public void setCab(Cab cab) {
        this.cab = cab;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Integer getDropSequence() {
        return dropSequence;
    }

    public void setDropSequence(Integer dropSequence) {
        this.dropSequence = dropSequence;
    }

    public boolean getAlertSent() {
        return alertSent;
    }

    public void setAlertSent(boolean alertSent) {
        this.alertSent = alertSent;
    }

    public LocalDateTime getBoardedAt() {
        return boardedAt;
    }

    public void setBoardedAt(LocalDateTime boardedAt) {
        this.boardedAt = boardedAt;
    }

    public LocalDateTime getDroppedAt() {
        return droppedAt;
    }

    public void setDroppedAt(LocalDateTime droppedAt) {
        this.droppedAt = droppedAt;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
