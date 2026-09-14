package com.logistics.DTO;

import com.logistics.entity.LoadedTrip;
import com.logistics.entity.Trip;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripResponse toResponse(Trip trip) {
        if (trip == null) {
            return null;
        }

        TripResponse.TripResponseBuilder builder = TripResponse.builder()
                .id(trip.getId())
                .legType(trip.getLegType())
                .status(trip.getStatus())
                .driverId(trip.getDriver() != null ? trip.getDriver().getId() : null)
                .loadId(trip.getLoad() != null ? trip.getLoad().getId() : null)
                .vehicleId(trip.getVehicle() != null ? trip.getVehicle().getId() : null)
                .customerId(trip.getCustomer() != null ? trip.getCustomer().getId() : null)
                .plateNumber(trip.getPlateNumber())
                .destination(trip.getDestination())
                .startMileage(trip.getStartMileage())
                .endMileage(trip.getEndMileage())
                .startTime(trip.getStartTime())
                .endTime(trip.getEndTime())
                .dateTime(trip.getDateTime())
                .tripGroupId(trip.getTripGroupId());

        if (trip instanceof LoadedTrip loaded) {
            builder.fuelLitres(loaded.getFuelLitres())
                    .trailer1(loaded.getTrailer1())
                    .trailer2(loaded.getTrailer2())
                    .deadheadTripId(loaded.getDeadheadTrip() != null ? loaded.getDeadheadTrip().getId() : null);
        }

        return builder.build();
    }

    public DriverActiveTripResponse toActiveTrip(Trip trip) {
        if (trip == null) {
            return null;
        }
        return DriverActiveTripResponse.builder()
                .id(trip.getId())
                .leg(trip.getLegType())
                .loadId(trip.getLoad() != null ? trip.getLoad().getId() : null)
                .startMileage(trip.getStartMileage())
                .status(trip.getStatus())
                .build();
    }
}
