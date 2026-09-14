package com.logistics.service.Impl;

import com.logistics.DTO.DeadheadEndResponse;
import com.logistics.DTO.EndDeadheadRequest;
import com.logistics.DTO.EndLoadedTripRequest;
import com.logistics.DTO.StartDeadheadRequest;
import com.logistics.DTO.TripMapper;
import com.logistics.DTO.TripResponse;
import com.logistics.entity.Customer;
import com.logistics.entity.DeadheadTrip;
import com.logistics.entity.Driver;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.Trip;
import com.logistics.entity.TripDTO;
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
import com.logistics.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final DeadheadTripRepository deadheadTripRepository;
    private final LoadedTripRepository loadedTripRepository;
    private final DriverRepository driverRepository;
    private final LoadRepository loadRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final TripMapper tripMapper;

    public TripServiceImpl(TripRepository tripRepository,
                           DeadheadTripRepository deadheadTripRepository,
                           LoadedTripRepository loadedTripRepository,
                           DriverRepository driverRepository,
                           LoadRepository loadRepository,
                           VehicleRepository vehicleRepository,
                           CustomerRepository customerRepository,
                           TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.deadheadTripRepository = deadheadTripRepository;
        this.loadedTripRepository = loadedTripRepository;
        this.driverRepository = driverRepository;
        this.loadRepository = loadRepository;
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.tripMapper = tripMapper;
    }

    @Override
    @Transactional
    public TripResponse startDeadhead(StartDeadheadRequest request) {
        Driver driver = currentDriver();
        Vehicle vehicle = standingVehicle(driver);
        Load load = loadRepository.findById(request.getLoadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load not found"));

        if (tripRepository.existsByDriverIdAndStatus(driver.getId(), TripStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver already has an active trip");
        }

        LocalDateTime now = LocalDateTime.now();
        DeadheadTrip deadhead = new DeadheadTrip();
        applySharedStartFields(deadhead, driver, load, vehicle, request.getStartMileage(), now);
        deadhead.setTripGroupId(UUID.randomUUID().toString());
        deadhead.setDestination(load.getPickupLocation());

        load.setStatus(Load.STATUS_IN_TRANSIT);
        loadRepository.save(load);

        return tripMapper.toResponse(deadheadTripRepository.save(deadhead));
    }

    @Override
    @Transactional
    public DeadheadEndResponse endDeadhead(Long deadheadId, EndDeadheadRequest request) {
        Driver driver = currentDriver();
        DeadheadTrip deadhead = deadheadTripRepository.findById(deadheadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadhead trip not found"));

        assertOwnedBy(deadhead, driver);
        assertActive(deadhead, "Deadhead trip is not active");
        validateEndMileage(deadhead.getStartMileage(), request.getEndMileage());

        LocalDateTime now = LocalDateTime.now();
        deadhead.setEndMileage(request.getEndMileage());
        deadhead.setEndTime(now);
        deadhead.setStatus(TripStatus.COMPLETED);
        DeadheadTrip completedDeadhead = deadheadTripRepository.save(deadhead);

        LoadedTrip loaded = new LoadedTrip();
        applySharedStartFields(
                loaded,
                completedDeadhead.getDriver(),
                completedDeadhead.getLoad(),
                completedDeadhead.getVehicle(),
                request.getEndMileage(),
                now
        );
        loaded.setTripGroupId(completedDeadhead.getTripGroupId());
        loaded.setDestination(completedDeadhead.getLoad() != null
                ? completedDeadhead.getLoad().getDeliveryLocation()
                : completedDeadhead.getDestination());
        loaded.setDeadheadTrip(completedDeadhead);
        LoadedTrip startedLoaded = loadedTripRepository.save(loaded);

        return new DeadheadEndResponse(
                tripMapper.toResponse(completedDeadhead),
                tripMapper.toResponse(startedLoaded)
        );
    }

    @Override
    @Transactional
    public TripResponse endLoaded(Long loadedId, EndLoadedTripRequest request) {
        Driver driver = currentDriver();
        LoadedTrip loaded = loadedTripRepository.findById(loadedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loaded trip not found"));

        assertOwnedBy(loaded, driver);
        assertActive(loaded, "Loaded trip is not active");
        validateEndMileage(loaded.getStartMileage(), request.getEndMileage());

        loaded.setEndMileage(request.getEndMileage());
        loaded.setEndTime(LocalDateTime.now());
        loaded.setStatus(TripStatus.COMPLETED);
        loaded.setFuelLitres(request.getFuelLitres());
        loaded.setTrailer1(request.getTrailer1());
        loaded.setTrailer2(request.getTrailer2());

        Load load = loaded.getLoad();
        if (load != null) {
            load.setStatus(Load.STATUS_DELIVERED);
            loadRepository.save(load);
        }

        return tripMapper.toResponse(loadedTripRepository.save(loaded));
    }

    @Override
    @Transactional
    public LoadedTrip createLoadedTripFromDto(TripDTO tripDTO) {
        Driver driver = driverRepository.findById(tripDTO.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
        Load load = loadRepository.findById(tripDTO.getLoadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load not found"));
        Customer customer = customerRepository.findById(tripDTO.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        validateEndMileage(tripDTO.getStartingMillage(), tripDTO.getEndingMillage());

        LocalDateTime start = tripDTO.getDateTime() != null ? tripDTO.getDateTime() : LocalDateTime.now();
        LoadedTrip trip = new LoadedTrip();
        trip.setDateTime(start);
        trip.setStartTime(start);
        trip.setEndTime(start);
        trip.setDestination(tripDTO.getDestination());
        trip.setStartMileage(tripDTO.getStartingMillage());
        trip.setEndMileage(tripDTO.getEndingMillage());
        trip.setFuelLitres(tripDTO.getFuelLitres());
        trip.setTrailer1(tripDTO.getTrailer1());
        trip.setTrailer2(tripDTO.getTrailer2());
        trip.setDriver(driver);
        trip.setLoad(load);
        trip.setPlateNumber(tripDTO.getPlateNumber());
        trip.setCustomer(customer);
        trip.setStatus(TripStatus.COMPLETED);
        trip.setTripGroupId(UUID.randomUUID().toString());

        Vehicle vehicle = vehicleRepository.findByDriver(driver).orElse(driver.getVehicle());
        if (vehicle == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver has no standing vehicle assignment");
        }
        trip.setVehicle(vehicle);
        if (trip.getPlateNumber() == null) {
            trip.setPlateNumber(vehicle.getLicensePlate());
        }

        return loadedTripRepository.save(trip);
    }

    @Override
    public Trip saveTrip(Trip trip) {
        return tripRepository.save(trip);
    }

    @Override
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    @Override
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    @Override
    public void deleteTrip(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
        }
        tripRepository.deleteById(id);
    }

    @Override
    public List<Trip> getTripsByDriverId(Long driverId) {
        return tripRepository.findByDriverId(driverId);
    }

    private void applySharedStartFields(Trip trip,
                                        Driver driver,
                                        Load load,
                                        Vehicle vehicle,
                                        Double startMileage,
                                        LocalDateTime now) {
        trip.setDriver(driver);
        trip.setLoad(load);
        trip.setVehicle(vehicle);
        trip.setPlateNumber(vehicle.getLicensePlate());
        trip.setCustomer(resolveCustomer(load));
        trip.setStartMileage(startMileage);
        trip.setStartTime(now);
        trip.setDateTime(now);
        trip.setStatus(TripStatus.ACTIVE);
    }

    private Customer resolveCustomer(Load load) {
        if (load.getCustomer() != null) {
            return load.getCustomer();
        }
        if (load.getCustomerId() != null) {
            return customerRepository.findById(load.getCustomerId()).orElse(null);
        }
        return null;
    }

    private Driver currentDriver() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return driverRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is not a driver"));
    }

    private Vehicle standingVehicle(Driver driver) {
        return vehicleRepository.findByDriver(driver)
                .or(() -> Optional.ofNullable(driver.getVehicle()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Driver has no standing vehicle assignment"));
    }

    private void assertOwnedBy(Trip trip, Driver driver) {
        if (trip.getDriver() == null || !trip.getDriver().getId().equals(driver.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip does not belong to the authenticated driver");
        }
    }

    private void assertActive(Trip trip, String message) {
        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateEndMileage(Double startMileage, Double endMileage) {
        if (endMileage == null) {
            throw new InvalidMileageException("endMileage is required");
        }
        if (startMileage != null && endMileage < startMileage) {
            throw new InvalidMileageException("endMileage must not be lower than startMileage");
        }
    }
}
