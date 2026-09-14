package com.logistics.controllers;

import com.logistics.entity.LoadedTrip;
import com.logistics.entity.Trip;
import com.logistics.entity.TripDTO;
import com.logistics.entity.User;
import com.logistics.repository.UserRepository;
import com.logistics.service.Impl.CustomUserDetailsService;
import com.logistics.service.TripService;
import com.logistics.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/driver-trips")
public class DriverTripController {
    private final TripService tripService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;

    public DriverTripController(TripService tripService, JwtUtil jwtUtil,
                                CustomUserDetailsService customUserDetailsService,
                                UserRepository userRepository) {
        this.tripService = tripService;
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Trip createDriverTrip(@RequestBody LoadedTrip driverTrip) {
        return tripService.saveTrip(driverTrip);
    }

    @GetMapping
    public List<Trip> getAllDriverTrips() {
        return tripService.getAllTrips();
    }

    @GetMapping("/{id}")
    public Trip getDriverTripById(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDriverTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getTripsByDriverId(
            @PathVariable Long driverId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String authenticatedUsername;
            try {
                authenticatedUsername = jwtUtil.extractUsername(token);
            } catch (JwtException e) {
                log.error("JWT token extraction failed", e);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            UserDetails userDetails;
            try {
                userDetails = customUserDetailsService.loadUserByUsername(authenticatedUsername);
            } catch (UsernameNotFoundException e) {
                log.error("User not found: {}", authenticatedUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
            }

            if (!jwtUtil.validateToken(token, userDetails)) {
                log.warn("Token validation failed for user: {}", authenticatedUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token validation failed");
            }

            Long authenticatedDriverId = convertUsernameToDriverId(authenticatedUsername);

            if (authenticatedDriverId == null || !authenticatedDriverId.equals(driverId)) {
                log.warn("Unauthorized access attempt. Requested ID: {}, Authenticated ID: {}",
                        driverId, authenticatedDriverId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
            }

            List<Trip> trips = tripService.getTripsByDriverId(driverId);
            return trips.isEmpty()
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok(trips);

        } catch (Exception e) {
            log.error("Unexpected error in getTripsByDriverId", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    private Long convertUsernameToDriverId(String username) {
        try {
            Optional<User> driver = userRepository.findByUsername(username);
            return driver.map(User::getId).orElse(null);
        } catch (Exception e) {
            log.error("Error converting username to driver ID", e);
            return null;
        }
    }

    @GetMapping("/driver/username/{username}")
    public ResponseEntity<?> getTripsByUsername(@PathVariable String username, @RequestHeader("Authorization") String token) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Driver not found"));
        return getTripsByDriverId(driver.getId(), token);
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<LoadedTrip> createTrip(@RequestBody TripDTO tripDTO) {
        return ResponseEntity.ok(tripService.createLoadedTripFromDto(tripDTO));
    }
}
