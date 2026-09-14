package com.logistics.service;

import com.logistics.DTO.DeadheadEndResponse;
import com.logistics.DTO.EndDeadheadRequest;
import com.logistics.DTO.EndLoadedTripRequest;
import com.logistics.DTO.StartDeadheadRequest;
import com.logistics.DTO.TripResponse;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.Trip;
import com.logistics.entity.TripDTO;

import java.util.List;

public interface TripService {
    TripResponse startDeadhead(StartDeadheadRequest request);

    DeadheadEndResponse endDeadhead(Long deadheadId, EndDeadheadRequest request);

    TripResponse endLoaded(Long loadedId, EndLoadedTripRequest request);

    LoadedTrip createLoadedTripFromDto(TripDTO tripDTO);

    Trip saveTrip(Trip trip);

    List<Trip> getAllTrips();

    Trip getTripById(Long id);

    void deleteTrip(Long id);

    List<Trip> getTripsByDriverId(Long driverId);
}
