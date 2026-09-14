package com.logistics.service.Impl;

import com.logistics.DTO.EndDeadheadRequest;
import com.logistics.DTO.EndLoadedTripRequest;
import com.logistics.DTO.StartDeadheadRequest;
import com.logistics.DTO.TripMapper;
import com.logistics.entity.DeadheadTrip;
import com.logistics.entity.Driver;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.exception.InvalidMileageException;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.DeadheadTripRepository;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.LoadedTripRepository;
import com.logistics.repository.TripRepository;
import com.logistics.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock private TripRepository tripRepository;
    @Mock private DeadheadTripRepository deadheadTripRepository;
    @Mock private LoadedTripRepository loadedTripRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private LoadRepository loadRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerRepository customerRepository;
    @Spy private TripMapper tripMapper = new TripMapper();

    @InjectMocks
    private TripServiceImpl tripService;

    private Driver driver;
    private Vehicle vehicle;
    private Load load;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(10L);
        driver.setUsername("drv");

        vehicle = new Vehicle();
        vehicle.setId(20L);
        vehicle.setLicensePlate("ABC123");

        load = new Load();
        load.setId(30L);
        load.setPickupLocation("Depot");
        load.setDeliveryLocation("Customer site");

        var principal = org.springframework.security.core.userdetails.User
                .withUsername("drv")
                .password("secret")
                .authorities("DRIVER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void startDeadheadUsesStandingVehicleAssignment() {
        StartDeadheadRequest request = new StartDeadheadRequest();
        request.setLoadId(30L);
        request.setStartMileage(1000.0);

        when(driverRepository.findByUsername("drv")).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByDriver(driver)).thenReturn(Optional.of(vehicle));
        when(loadRepository.findById(30L)).thenReturn(Optional.of(load));
        when(tripRepository.existsByDriverIdAndStatus(10L, TripStatus.ACTIVE)).thenReturn(false);
        when(deadheadTripRepository.save(any(DeadheadTrip.class))).thenAnswer(invocation -> {
            DeadheadTrip trip = invocation.getArgument(0);
            trip.setId(1L);
            return trip;
        });

        var response = tripService.startDeadhead(request);

        assertEquals(1L, response.getId());
        assertEquals(20L, response.getVehicleId());
        assertEquals("ABC123", response.getPlateNumber());
        assertEquals("Depot", response.getDestination());
        assertEquals(TripStatus.ACTIVE, response.getStatus());
        assertEquals(1000.0, response.getStartMileage());
        assertNotNull(response.getTripGroupId());
    }

    @Test
    void endDeadheadRejectsLowerEndMileage() {
        DeadheadTrip deadhead = activeDeadhead(1000.0);
        EndDeadheadRequest request = new EndDeadheadRequest();
        request.setEndMileage(999.0);

        when(driverRepository.findByUsername("drv")).thenReturn(Optional.of(driver));
        when(deadheadTripRepository.findById(1L)).thenReturn(Optional.of(deadhead));

        InvalidMileageException ex = assertThrows(InvalidMileageException.class,
                () -> tripService.endDeadhead(1L, request));
        assertEquals("endMileage must not be lower than startMileage", ex.getMessage());
        verify(loadedTripRepository, never()).save(any());
    }

    @Test
    void endDeadheadCompletesAndStartsLoadedTrip() {
        DeadheadTrip deadhead = activeDeadhead(1000.0);
        EndDeadheadRequest request = new EndDeadheadRequest();
        request.setEndMileage(1100.0);

        when(driverRepository.findByUsername("drv")).thenReturn(Optional.of(driver));
        when(deadheadTripRepository.findById(1L)).thenReturn(Optional.of(deadhead));
        when(deadheadTripRepository.save(any(DeadheadTrip.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loadedTripRepository.save(any(LoadedTrip.class))).thenAnswer(invocation -> {
            LoadedTrip trip = invocation.getArgument(0);
            trip.setId(2L);
            return trip;
        });

        var response = tripService.endDeadhead(1L, request);

        assertEquals(TripStatus.COMPLETED, response.getDeadheadTrip().getStatus());
        assertEquals(1100.0, response.getDeadheadTrip().getEndMileage());
        assertEquals(TripStatus.ACTIVE, response.getLoadedTrip().getStatus());
        assertEquals(1100.0, response.getLoadedTrip().getStartMileage());
        assertEquals(1L, response.getLoadedTrip().getDeadheadTripId());
        assertEquals("Customer site", response.getLoadedTrip().getDestination());
        assertEquals(response.getDeadheadTrip().getTripGroupId(), response.getLoadedTrip().getTripGroupId());

        ArgumentCaptor<LoadedTrip> loadedCaptor = ArgumentCaptor.forClass(LoadedTrip.class);
        verify(loadedTripRepository).save(loadedCaptor.capture());
        assertEquals(deadhead, loadedCaptor.getValue().getDeadheadTrip());
    }

    @Test
    void endLoadedRejectsLowerEndMileage() {
        LoadedTrip loaded = new LoadedTrip();
        loaded.setId(2L);
        loaded.setDriver(driver);
        loaded.setStartMileage(1100.0);
        loaded.setStatus(TripStatus.ACTIVE);

        EndLoadedTripRequest request = new EndLoadedTripRequest();
        request.setEndMileage(1099.0);
        request.setFuelLitres(80.0);
        request.setTrailer1("T-1");
        request.setTrailer2("T-2");

        when(driverRepository.findByUsername("drv")).thenReturn(Optional.of(driver));
        when(loadedTripRepository.findById(2L)).thenReturn(Optional.of(loaded));

        assertThrows(InvalidMileageException.class, () -> tripService.endLoaded(2L, request));
        verify(loadedTripRepository, never()).save(any());
    }

    @Test
    void endLoadedPersistsFuelAndBothTrailers() {
        LoadedTrip loaded = new LoadedTrip();
        loaded.setId(2L);
        loaded.setDriver(driver);
        loaded.setStartMileage(1100.0);
        loaded.setStatus(TripStatus.ACTIVE);

        EndLoadedTripRequest request = new EndLoadedTripRequest();
        request.setEndMileage(1400.0);
        request.setFuelLitres(80.5);
        request.setTrailer1("T-1");
        request.setTrailer2("T-2");

        when(driverRepository.findByUsername("drv")).thenReturn(Optional.of(driver));
        when(loadedTripRepository.findById(2L)).thenReturn(Optional.of(loaded));
        when(loadedTripRepository.save(any(LoadedTrip.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = tripService.endLoaded(2L, request);

        assertEquals(TripStatus.COMPLETED, response.getStatus());
        assertEquals(1400.0, response.getEndMileage());
        assertEquals(80.5, response.getFuelLitres());
        assertEquals("T-1", response.getTrailer1());
        assertEquals("T-2", response.getTrailer2());
    }

    private DeadheadTrip activeDeadhead(double startMileage) {
        DeadheadTrip deadhead = new DeadheadTrip();
        deadhead.setId(1L);
        deadhead.setDriver(driver);
        deadhead.setLoad(load);
        deadhead.setVehicle(vehicle);
        deadhead.setStartMileage(startMileage);
        deadhead.setStatus(TripStatus.ACTIVE);
        deadhead.setTripGroupId("group-1");
        deadhead.setStartTime(LocalDateTime.now().minusHours(1));
        return deadhead;
    }
}
