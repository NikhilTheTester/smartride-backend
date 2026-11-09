package com.smartride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Smart Ride backend.
 * Right click this file in Eclipse -> Run As -> Java Application (or Spring Boot App).
 */
@SpringBootApplication
public class SmartRideApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartRideApplication.class, args);
    }
}
