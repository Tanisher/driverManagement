

package com.logistics.service;

import com.logistics.DTO.VehicleDTO;
import com.logistics.entity.Fault;
import com.logistics.entity.Vehicle;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VehicleService {
    List<Vehicle> getAllVehicles();
    Optional<Vehicle> getVehicleById(Long id);
    Vehicle createVehicle(Vehicle vehicle);
    Vehicle updateVehicle(Long id, Vehicle vehicle);
    Vehicle patchVehicle(Long id, VehicleDTO vehicleDTO);
    void deleteVehicle(Long id);
    List<Fault> getFaultsByVehicleId(Long vehicleId);
    void updateVehicleLocation(Long vehicleId, Double latitude, Double longitude);
    Vehicle findById(Long vehicleId);
    Vehicle assignDriverToVehicle(Long vehicleId, Long driverId);
    Vehicle unassignDriverFromVehicle(Long vehicleId);
    Map<String, Double> getVehicleLocationByDriver(Long driverId);
}