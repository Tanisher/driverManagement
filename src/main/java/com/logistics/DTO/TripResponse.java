package com.logistics.DTO;

import com.logistics.entity.TripLegType;
import com.logistics.entity.TripStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TripResponse {

    private Long id;
    private TripLegType legType;
    private TripStatus status;
    private Long driverId;
    private Long loadId;
    private Long vehicleId;
    private Long customerId;
    private String plateNumber;
    private String destination;
    private Double startMileage;
    private Double endMileage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** Carried over from DriverTrip.dateTime; equals startTime. */
    private LocalDateTime dateTime;
    private String tripGroupId;
    private Long deadheadTripId;
    private Double fuelLitres;
    private String trailer1;
    private String trailer2;
}
