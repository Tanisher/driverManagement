package com.logistics.repository;

import com.logistics.entity.Trip;
import com.logistics.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverId(Long driverId);

    boolean existsByDriverIdAndStatus(Long driverId, TripStatus status);

    long countByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t JOIN FETCH t.vehicle WHERE t.status = :status")
    List<Trip> findByStatusWithVehicle(@Param("status") TripStatus status);
}
