package com.logistics.controllers;

import com.logistics.entity.*;
import com.logistics.repository.*;
import com.logistics.service.DriverTripService;
import com.logistics.service.Impl.CustomUserDetailsService;
import com.logistics.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/driver-trips")
public class DriverTripController {
    private final DriverTripService driverTripService;
    private final JwtUtil jwtUtil;
    private final DriverRepository driverRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final LoadRepository loadRepository;
    private final CustomerRepository customerRepository;
    private final DriverTripRepository driverTripRepository;

    public DriverTripController(DriverTripService driverTripService, JwtUtil jwtUtil,
                                DriverRepository driverRepository, CustomUserDetailsService customUserDetailsService,
                                UserRepository userRepository, LoadRepository loadRepository,
                                CustomerRepository customerRepository, DriverTripRepository driverTripRepository) {
        this.driverTripService = driverTripService;
        this.jwtUtil = jwtUtil;
        this.driverRepository = driverRepository;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.loadRepository = loadRepository;
        this.customerRepository = customerRepository;
        this.driverTripRepository = driverTripRepository;
    }

    @PostMapping
    public DriverTrip createDriverTrip(@RequestBody DriverTrip driverTrip) {
        return driverTripService.saveDriverTrip(driverTrip);
    }

    @GetMapping
    public List<DriverTrip> getAllDriverTrips() {
        return driverTripService.getAllDriverTrips();
    }

    @GetMapping("/{id}")
    public DriverTrip getDriverTripById(@PathVariable Long id) {
        return driverTripService.getDriverTripById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDriverTrip(@PathVariable Long id) {
        driverTripService.deleteDriverTrip(id);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getTripsByDriverId(
            @PathVariable Long driverId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Extract username first
            String authenticatedUsername;
            try {
                authenticatedUsername = jwtUtil.extractUsername(token);
            } catch (JwtException e) {
                log.error("JWT token extraction failed", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // Load user details
            UserDetails userDetails;
            try {
                userDetails = customUserDetailsService.loadUserByUsername(authenticatedUsername);
            } catch (UsernameNotFoundException e) {
                log.error("User not found: {}", authenticatedUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            // Validate token
            if (!jwtUtil.validateToken(token, userDetails)) {
                log.warn("Token validation failed for user: {}", authenticatedUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token validation failed");
            }

            // Convert username to driver ID
            Long authenticatedDriverId = convertUsernameToDriverId(authenticatedUsername);

            // Check if the authenticated driver matches the requested driver ID
            if (authenticatedDriverId == null || !authenticatedDriverId.equals(driverId)) {
                log.warn("Unauthorized access attempt. Requested ID: {}, Authenticated ID: {}",
                        driverId, authenticatedDriverId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
            }

            // Retrieve trips
            List<DriverTrip> trips = driverTripService.getTripsByDriverId(driverId);
            return trips.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(trips);

        } catch (Exception e) {
            log.error("Unexpected error in getTripsByDriverId", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    // Method to convert username to driver ID
    private Long convertUsernameToDriverId(String username) {
        try {
            // Find driver by username
            Optional<User> driver = userRepository.findByUsername(username);
            return driver.map(User::getId).orElse(null);
        } catch (Exception e) {
            // Log the error
            log.error("Error converting username to driver ID", e);
            return null;
        }
    }

    @GetMapping("/driver/username/{username}")
    public ResponseEntity<?> getTripsByUsername(@PathVariable String username,  @RequestHeader("Authorization") String token) {

        // Find driver by username first, then get ID
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Driver not found"));



        ResponseEntity<?> response = getTripsByDriverId(driver.getId(), token);

        // Debug print response status
        System.out.println("Response status: " + response.getStatusCode());
        return getTripsByDriverId(driver.getId(), token );
    }


    //Addition of trip
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<DriverTrip> createTrip(@RequestBody TripDTO tripDTO) {
        // Fetch related entities
        Driver driver = driverRepository.findById(tripDTO.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Driver not found"));

        Load load = loadRepository.findById(tripDTO.getLoadId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Load not found"));

        Customer customer = customerRepository.findById(tripDTO.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        // Map DTO to entity
        DriverTrip trip = new DriverTrip();
        trip.setDateTime(tripDTO.getDateTime());
        trip.setDestination(tripDTO.getDestination());
        trip.setStartingMillage(tripDTO.getStartingMillage());
        trip.setEndingMillage(tripDTO.getEndingMillage());
        trip.setFuelLitres(tripDTO.getFuelLitres());
        trip.setTrailer1(tripDTO.getTrailer1());
        trip.setTrailer2(tripDTO.getTrailer2());
        trip.setDriver(driver);
        trip.setLoad(load);
        trip.setPlateNumber(tripDTO.getPlateNumber());
        trip.setCustomer(customer);

        // Save the trip
        DriverTrip savedTrip = driverTripRepository.save(trip);

        return ResponseEntity.ok(savedTrip);
    }


}

