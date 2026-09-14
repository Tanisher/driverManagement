package com.logistics.service.Impl;

import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.TripRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private TripRepository tripRepository;
    @Mock private LoadRepository loadRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void overviewCountsActiveTripsVehiclesAndLoadsByStatus() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setLicensePlate("ABC123");
        vehicle.setLatitude(-17.8);
        vehicle.setLongitude(31.0);

        LoadedTrip trip = new LoadedTrip();
        trip.setId(9L);
        trip.setStatus(TripStatus.ACTIVE);
        trip.setVehicle(vehicle);

        when(tripRepository.countByStatus(TripStatus.ACTIVE)).thenReturn(2L);
        when(tripRepository.findByStatusWithVehicle(TripStatus.ACTIVE)).thenReturn(List.of(trip));
        when(loadRepository.countByStatus(Load.STATUS_PENDING)).thenReturn(4L);
        when(loadRepository.countByStatus(Load.STATUS_IN_TRANSIT)).thenReturn(3L);
        when(loadRepository.countByStatus(Load.STATUS_DELIVERED)).thenReturn(8L);

        var overview = dashboardService.getOverview();

        assertEquals(2L, overview.getActiveTripCount());
        assertEquals(1, overview.getVehiclesOnActiveTrips().size());
        assertEquals(5L, overview.getVehiclesOnActiveTrips().get(0).getVehicleId());
        assertEquals(-17.8, overview.getVehiclesOnActiveTrips().get(0).getLatitude());
        assertEquals(4L, overview.getLoadsByStatus().getPending());
        assertEquals(3L, overview.getLoadsByStatus().getInTransit());
        assertEquals(8L, overview.getLoadsByStatus().getDelivered());
    }
}
