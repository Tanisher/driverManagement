package com.logistics.service.Impl;

import com.logistics.entity.Driver;
import com.logistics.entity.Fault;
import com.logistics.entity.Vehicle;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.FaultRepository;
import com.logistics.repository.VehicleRepository;
import com.logistics.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);


    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FaultRepository faultRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException("Vehicle with the same license plate already exists.");
        }
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found."));

        if (vehicle.getLicensePlate() != null
                && !vehicle.getLicensePlate().equals(existingVehicle.getLicensePlate())
                && vehicleRepository.existsByLicensePlateAndIdNot(vehicle.getLicensePlate(), id)) {
            throw new IllegalArgumentException("Vehicle with the same license plate already exists.");
        }

        existingVehicle.setLicensePlate(vehicle.getLicensePlate());
        existingVehicle.setMake(vehicle.getMake());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setYear(vehicle.getYear());
        existingVehicle.setColor(vehicle.getColor());
        existingVehicle.setActive(vehicle.isActive());
        existingVehicle.setLastServiceDate(vehicle.getLastServiceDate());
        existingVehicle.setVehicleType(vehicle.getVehicleType());

        return vehicleRepository.save(existingVehicle);
    }

    @Override
    public Vehicle patchVehicle(Long id, com.logistics.DTO.VehicleDTO dto) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found."));
        if (dto.getLicensePlate() != null) {
            if (!dto.getLicensePlate().equals(existing.getLicensePlate())
                    && vehicleRepository.existsByLicensePlateAndIdNot(dto.getLicensePlate(), id)) {
                throw new IllegalArgumentException("Vehicle with the same license plate already exists.");
            }
            existing.setLicensePlate(dto.getLicensePlate());
        }
        if (dto.getMake() != null) {
            existing.setMake(dto.getMake());
        }
        if (dto.getModel() != null) {
            existing.setModel(dto.getModel());
        }
        if (dto.getYear() != 0) {
            existing.setYear(dto.getYear());
        }
        if (dto.getColor() != null) {
            existing.setColor(dto.getColor());
        }
        if (dto.getLastServiceDate() != null) {
            existing.setLastServiceDate(dto.getLastServiceDate());
        }
        if (dto.getVehicleType() != null) {
            existing.setVehicleType(dto.getVehicleType());
        }
        if (dto.getLatitude() != null) {
            existing.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            existing.setLongitude(dto.getLongitude());
        }
        return vehicleRepository.save(existing);
    }

    @Override
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    public List<Fault> getFaultsByVehicleId(Long vehicleId) {
        return faultRepository.findByVehicleId(vehicleId);
    }

    @Override
    public void updateVehicleLocation(Long vehicleId, Double latitude, Double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setLatitude(latitude);
        vehicle.setLongitude(longitude);
        vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle findById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    @Override
    @Transactional
    public Vehicle assignDriverToVehicle(Long vehicleId, Long driverId) {
        if (vehicleId == null || driverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle ID and Driver ID must not be null");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vehicle not found with id: " + vehicleId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Driver not found with id: " + driverId));

        assertVehicleNotAssignedToAnotherDriver(vehicle, driverId);

        if (vehicle.getDriver() != null && vehicle.getDriver().getId().equals(driverId)) {
            return vehicle;
        }

        vehicleRepository.findByDriver(driver).ifPresent(existingVehicle -> {
            if (!existingVehicle.getId().equals(vehicle.getId())) {
                existingVehicle.setDriver(null);
                vehicleRepository.save(existingVehicle);
            }
        });

        vehicle.setDriver(driver);
        return vehicleRepository.save(vehicle);
    }

    private void assertVehicleNotAssignedToAnotherDriver(Vehicle vehicle, Long driverId) {
        Driver currentDriver = vehicle.getDriver();
        if (currentDriver != null && currentDriver.getId() != null && !currentDriver.getId().equals(driverId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vehicle is already assigned to another driver");
        }
    }

    @Override
    public Vehicle unassignDriverFromVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setDriver(null);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Map<String, Double> getVehicleLocationByDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + driverId));

        Vehicle vehicle = vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("No vehicle assigned to driver with id: " + driverId));

        Map<String, Double> location = new HashMap<>();
        location.put("latitude", vehicle.getLatitude());
        location.put("longitude", vehicle.getLongitude());

        return location;
    }
}
