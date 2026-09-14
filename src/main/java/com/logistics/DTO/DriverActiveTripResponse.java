package com.logistics.DTO;

import com.logistics.entity.TripLegType;
import com.logistics.entity.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Current in-progress leg for the authenticated driver, for mobile state recovery.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverActiveTripResponse {
    private Long id;
    private TripLegType leg;
    private Long loadId;
    private Double startMileage;
    private TripStatus status;
}
