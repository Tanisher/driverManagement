package com.logistics.controllers;

import com.logistics.DTO.DriverActiveTripResponse;
import com.logistics.DTO.DriverMapper;
import com.logistics.entity.TripLegType;
import com.logistics.entity.TripStatus;
import com.logistics.service.DriverService;
import com.logistics.service.TripService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock private DriverService driverService;
    @Mock private DriverMapper driverMapper;
    @Mock private TripService tripService;

    @InjectMocks
    private DriverController driverController;

    @Test
    void getMyActiveTripReturns204WhenNone() {
        when(tripService.findActiveTripForCurrentDriver()).thenReturn(Optional.empty());

        ResponseEntity<DriverActiveTripResponse> response = driverController.getMyActiveTrip();

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void getMyActiveTripReturns200WithCurrentLeg() {
        DriverActiveTripResponse body = DriverActiveTripResponse.builder()
                .id(1L)
                .leg(TripLegType.LOADED)
                .loadId(30L)
                .startMileage(1100.0)
                .status(TripStatus.ACTIVE)
                .build();
        when(tripService.findActiveTripForCurrentDriver()).thenReturn(Optional.of(body));

        ResponseEntity<DriverActiveTripResponse> response = driverController.getMyActiveTrip();

        assertEquals(200, response.getStatusCode().value());
        assertSame(body, response.getBody());
        assertEquals(TripLegType.LOADED, response.getBody().getLeg());
    }
}
