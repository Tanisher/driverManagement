package com.logistics.service.Impl;

import com.logistics.DTO.ActiveTripVehicleResponse;
import com.logistics.DTO.DashboardOverviewResponse;
import com.logistics.DTO.LoadStatusCounts;
import com.logistics.entity.Load;
import com.logistics.entity.Trip;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.TripRepository;
import com.logistics.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final TripRepository tripRepository;
    private final LoadRepository loadRepository;

    public DashboardServiceImpl(TripRepository tripRepository, LoadRepository loadRepository) {
        this.tripRepository = tripRepository;
        this.loadRepository = loadRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {
        long activeTripCount = tripRepository.countByStatus(TripStatus.ACTIVE);
        List<Trip> activeTrips = tripRepository.findByStatusWithVehicle(TripStatus.ACTIVE);

        Map<Long, ActiveTripVehicleResponse> vehicles = new LinkedHashMap<>();
        for (Trip trip : activeTrips) {
            Vehicle vehicle = trip.getVehicle();
            if (vehicle == null || vehicle.getId() == null || vehicles.containsKey(vehicle.getId())) {
                continue;
            }
            vehicles.put(vehicle.getId(), ActiveTripVehicleResponse.builder()
                    .vehicleId(vehicle.getId())
                    .licensePlate(vehicle.getLicensePlate())
                    .tripId(trip.getId())
                    .legType(trip.getLegType())
                    .tripStatus(trip.getStatus())
                    .latitude(vehicle.getLatitude())
                    .longitude(vehicle.getLongitude())
                    .build());
        }

        LoadStatusCounts loadsByStatus = LoadStatusCounts.builder()
                .pending(loadRepository.countByStatus(Load.STATUS_PENDING))
                .inTransit(loadRepository.countByStatus(Load.STATUS_IN_TRANSIT))
                .delivered(loadRepository.countByStatus(Load.STATUS_DELIVERED))
                .build();

        return DashboardOverviewResponse.builder()
                .activeTripCount(activeTripCount)
                .vehiclesOnActiveTrips(new ArrayList<>(vehicles.values()))
                .loadsByStatus(loadsByStatus)
                .build();
    }
}
