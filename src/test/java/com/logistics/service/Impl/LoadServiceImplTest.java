package com.logistics.service.Impl;

import com.logistics.entity.CargoType;
import com.logistics.entity.Driver;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.LoadedTripRepository;
import com.logistics.repository.VehicleRepository;
import com.logistics.util.DriverAssignmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadServiceImplTest {

    @Mock private LoadRepository loadRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private LoadedTripRepository loadedTripRepository;

    @InjectMocks
    private LoadServiceImpl loadService;

    private Load load;
    private Driver driver;
    private Vehicle vehicle;
    private DriverAssignmentRequest request;

    @BeforeEach
    void setUp() {
        load = new Load();
        load.setId(1L);
        load.setCargoType(CargoType.BAGGED);

        driver = new Driver();
        driver.setId(10L);

        vehicle = new Vehicle();
        vehicle.setId(20L);
        vehicle.setVehicleType(CargoType.BULK);

        request = new DriverAssignmentRequest();
        request.setDriverId(10L);
    }

    @Test
    void assignDriverRejectsCargoVehicleMismatch() {
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByDriver(driver)).thenReturn(Optional.of(vehicle));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> loadService.assignDriver(1L, request));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Vehicle type BULK cannot carry cargo type BAGGED", ex.getReason());
        verify(loadRepository, never()).save(any());
    }

    @Test
    void assignDriverPersistsWhenTypesMatch() {
        vehicle.setVehicleType(CargoType.BAGGED);
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByDriver(driver)).thenReturn(Optional.of(vehicle));
        when(loadRepository.save(any(Load.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Load assigned = loadService.assignDriver(1L, request);

        assertEquals(driver, assigned.getAssignedDriver());
        verify(loadRepository).save(load);
    }

    @Test
    void getBillableAmountUsesLoadedTripDistanceForPerKm() {
        load.setPricingMode(PricingMode.PER_KM);
        load.setRatePerKm(new BigDecimal("5.00"));
        load.setCargoType(CargoType.BAGGED);
        load.setWeight("10");

        LoadedTrip loaded = new LoadedTrip();
        loaded.setStatus(TripStatus.COMPLETED);
        loaded.setStartMileage(200.0);
        loaded.setEndMileage(260.0);

        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(loadedTripRepository.findFirstByLoadIdAndStatusOrderByIdDesc(1L, TripStatus.COMPLETED))
                .thenReturn(Optional.of(loaded));

        var result = loadService.getBillableAmount(1L);

        // 5.00 × 60 km × 10 t
        assertEquals(new BigDecimal("3000.00"), result.getBillableAmount());
        assertEquals(new BigDecimal("60.0"), result.getLoadedKm());
        assertEquals(PricingMode.PER_KM, result.getPricingMode());
        assertEquals(false, result.isWeightEstimated());
    }

    @Test
    void setActualWeightPersistsWeighbridgeReadingOnBulkLoad() {
        load.setCargoType(CargoType.BULK);
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));
        when(loadRepository.save(any(Load.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Load updated = loadService.setActualWeight(1L, new BigDecimal("12.5"));

        assertEquals(new BigDecimal("12.5"), updated.getActualWeight());
        verify(loadRepository).save(load);
    }

    @Test
    void setActualWeightRejectsNonBulkLoad() {
        load.setCargoType(CargoType.BAGGED);
        when(loadRepository.findById(1L)).thenReturn(Optional.of(load));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> loadService.setActualWeight(1L, new BigDecimal("12.5")));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("actualWeight applies to BULK loads only", ex.getReason());
        verify(loadRepository, never()).save(any());
    }
}
