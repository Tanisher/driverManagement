package com.logistics.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private long activeTripCount;
    private List<ActiveTripVehicleResponse> vehiclesOnActiveTrips;
    private LoadStatusCounts loadsByStatus;
}
