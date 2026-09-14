package com.logistics.repository;

import com.logistics.entity.DeadheadTrip;
import com.logistics.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeadheadTripRepository extends JpaRepository<DeadheadTrip, Long> {

    Optional<DeadheadTrip> findByDriverIdAndLoadIdAndStatus(Long driverId, Long loadId, TripStatus status);
}
