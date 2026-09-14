package com.logistics.repository;

import com.logistics.entity.Trip;
import com.logistics.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverId(Long driverId);

    boolean existsByDriverIdAndStatus(Long driverId, TripStatus status);
}
