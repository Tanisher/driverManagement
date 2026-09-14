package com.logistics.service.Impl;

import com.logistics.DTO.FaultMapper;
import com.logistics.DTO.VehicleMapper;
import com.logistics.controllers.VehicleController;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.payload.VehicleLocationMessage;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.FaultRepository;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.TripRepository;
import com.logistics.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketLocationDashboardTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private FaultRepository faultRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private TripRepository tripRepository;
    @Mock private LoadRepository loadRepository;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private FaultMapper faultMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private VehicleController vehicleController;
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        vehicleController = new VehicleController(vehicleService, vehicleMapper, faultMapper);
        dashboardService = new DashboardServiceImpl(tripRepository, loadRepository);
    }

    @Test
    void dashboardOverviewReflectsCoordinatesAfterWebSocketOnlyUpdate() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setLicensePlate("ABC123");

        LoadedTrip trip = new LoadedTrip();
        trip.setId(9L);
        trip.setStatus(TripStatus.ACTIVE);
        trip.setVehicle(vehicle);

        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleLocationMessage message = new VehicleLocationMessage();
        message.setVehicleId(5L);
        message.setLatitude(-17.825);
        message.setLongitude(31.033);

        vehicleController.sendLocationUpdate(message);

        when(tripRepository.countByStatus(TripStatus.ACTIVE)).thenReturn(1L);
        when(tripRepository.findByStatusWithVehicle(TripStatus.ACTIVE)).thenReturn(List.of(trip));
        when(loadRepository.countByStatus(Load.STATUS_PENDING)).thenReturn(0L);
        when(loadRepository.countByStatus(Load.STATUS_IN_TRANSIT)).thenReturn(1L);
        when(loadRepository.countByStatus(Load.STATUS_DELIVERED)).thenReturn(0L);

        var overview = dashboardService.getOverview();

        assertEquals(-17.825, overview.getVehiclesOnActiveTrips().get(0).getLatitude());
        assertEquals(31.033, overview.getVehiclesOnActiveTrips().get(0).getLongitude());
    }
}
