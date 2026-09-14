package com.logistics.DTO;

import com.logistics.entity.TripLegType;
import com.logistics.entity.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveTripVehicleResponse {
    private Long vehicleId;
    private String licensePlate;
    private Long tripId;
    private TripLegType legType;
    private TripStatus tripStatus;
    private Double latitude;
    private Double longitude;
}
