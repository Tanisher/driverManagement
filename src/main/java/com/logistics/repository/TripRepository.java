package com.logistics.repository;

import com.logistics.entity.Trip;
import com.logistics.entity.TripStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverId(Long driverId);

    boolean existsByDriverIdAndStatus(Long driverId, TripStatus status);

    @Query("""
            SELECT t FROM Trip t
            LEFT JOIN FETCH t.load
            WHERE t.driver.id = :driverId AND t.status = :status
            """)
    Optional<Trip> findActiveTripByDriverId(@Param("driverId") Long driverId,
                                            @Param("status") TripStatus status);

    long countByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t JOIN FETCH t.vehicle WHERE t.status = :status")
    List<Trip> findByStatusWithVehicle(@Param("status") TripStatus status);

    @Query("""
            SELECT t.endMileage FROM Trip t
            WHERE t.vehicle.id = :vehicleId
              AND t.endMileage IS NOT NULL
            ORDER BY COALESCE(t.endTime, t.startTime) DESC, t.id DESC
            """)
    List<Double> findEndMileagesForVehicle(@Param("vehicleId") Long vehicleId, Pageable pageable);
}
