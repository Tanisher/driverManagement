package com.logistics.repository;

import com.logistics.entity.LoadedTrip;
import com.logistics.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoadedTripRepository extends JpaRepository<LoadedTrip, Long> {
    Optional<LoadedTrip> findFirstByLoadIdAndStatusOrderByIdDesc(Long loadId, TripStatus status);
}
