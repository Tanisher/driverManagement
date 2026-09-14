package com.logistics.repository;

import com.logistics.entity.LoadedTrip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadedTripRepository extends JpaRepository<LoadedTrip, Long> {
}
