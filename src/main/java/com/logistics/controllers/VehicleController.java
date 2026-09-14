package com.logistics.controllers;

import com.logistics.DTO.FaultDTO;
import com.logistics.DTO.VehicleDTO;
import com.logistics.DTO.FaultMapper;
import com.logistics.DTO.VehicleMapper;
import com.logistics.payload.VehicleLocationMessage;
import com.logistics.service.VehicleService;
import com.logistics.util.DriverAssignmentRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;
    private final FaultMapper faultMapper;

    @Autowired
    public VehicleController(VehicleService vehicleService,
                             VehicleMapper vehicleMapper,
                             FaultMapper faultMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
        this.faultMapper = faultMapper;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        return ResponseEntity.ok(
                vehicleService.getAllVehicles().stream()
                        .map(vehicleMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
                .map(vehicle -> ResponseEntity.ok(vehicleMapper.toDTO(vehicle)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@RequestBody VehicleDTO vehicleDTO) {
        var vehicle = vehicleMapper.toEntity(vehicleDTO);
        var createdVehicle = vehicleService.createVehicle(vehicle);
        return ResponseEntity.ok(vehicleMapper.toDTO(createdVehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO) {
        var vehicle = vehicleMapper.toEntity(vehicleDTO);
        var updatedVehicle = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(vehicleMapper.toDTO(updatedVehicle));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VehicleDTO> patchVehicle(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO) {
        var updatedVehicle = vehicleService.patchVehicle(id, vehicleDTO);
        return ResponseEntity.ok(vehicleMapper.toDTO(updatedVehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/faults")
    public ResponseEntity<List<FaultDTO>> getFaultsByVehicleId(@PathVariable Long id) {
        var faults = vehicleService.getFaultsByVehicleId(id);
        var faultDTOs = faults.stream()
                .map(faultMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(faultDTOs);
    }

    @PutMapping("/{vehicleId}/location")
    public ResponseEntity<String> updateVehicleLocation(
            @PathVariable Long vehicleId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        vehicleService.updateVehicleLocation(vehicleId, latitude, longitude);
        return ResponseEntity.ok("Vehicle location updated successfully");
    }

    @GetMapping("/{vehicleId}/location")
    public ResponseEntity<Map<String, Double>> getVehicleLocation(@PathVariable Long vehicleId) {
        var vehicle = vehicleService.findById(vehicleId);
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", vehicle.getLatitude());
        location.put("longitude", vehicle.getLongitude());
        return ResponseEntity.ok(location);
    }

    @MessageMapping("/vehicle/location")
    @SendTo("/topic/vehicleLocation")
    public VehicleLocationMessage sendLocationUpdate(VehicleLocationMessage message) {
        logger.info("Received location update: {}", message);
        return message;
    }

    @PutMapping("/{vehicleId}/assign-driver")
    public ResponseEntity<VehicleDTO> assignDriverToVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody DriverAssignmentRequest request) {
        var vehicle = vehicleService.assignDriverToVehicle(vehicleId, request.getDriverId());
        return ResponseEntity.ok(vehicleMapper.toDTO(vehicle));
    }

    @PutMapping("/{vehicleId}/unassign-driver")
    public ResponseEntity<VehicleDTO> unassignDriverFromVehicle(@PathVariable Long vehicleId) {
        var vehicle = vehicleService.unassignDriverFromVehicle(vehicleId);
        return ResponseEntity.ok(vehicleMapper.toDTO(vehicle));
    }

    @GetMapping("/location/by-driver/{driverId}")
    public ResponseEntity<Map<String, Double>> getVehicleLocationByDriver(@PathVariable Long driverId) {
        try {
            return ResponseEntity.ok(vehicleService.getVehicleLocationByDriver(driverId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}