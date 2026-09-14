package com.logistics.service.Impl;

import com.logistics.entity.Driver;
import com.logistics.entity.Vehicle;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.FaultRepository;
import com.logistics.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private FaultRepository faultRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Test
    void assignDriverRejectsVehicleAlreadyAssignedToAnotherDriver() {
        Driver otherDriver = new Driver();
        otherDriver.setId(99L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setDriver(otherDriver);

        Driver incoming = new Driver();
        incoming.setId(10L);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(incoming));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> vehicleService.assignDriverToVehicle(1L, 10L));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Vehicle is already assigned to another driver", ex.getReason());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void assignDriverPersistsStandingAssignmentWhenVehicleIsFree() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);

        Driver driver = new Driver();
        driver.setId(10L);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByDriver(driver)).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle assigned = vehicleService.assignDriverToVehicle(1L, 10L);

        assertEquals(driver, assigned.getDriver());
        assertEquals(vehicle, driver.getVehicle());
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void updateVehicleLocationPersistsLatitudeAndLongitude() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);

        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        vehicleService.updateVehicleLocation(5L, -17.825, 31.033);

        assertEquals(-17.825, vehicle.getLatitude());
        assertEquals(31.033, vehicle.getLongitude());
        verify(vehicleRepository).save(vehicle);
    }
}
