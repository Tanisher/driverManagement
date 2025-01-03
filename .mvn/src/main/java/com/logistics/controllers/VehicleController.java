package com.logistics.controllers;

import com.logistics.entity.Driver;
import com.logistics.entity.Fault;
import com.logistics.entity.Vehicle;
import com.logistics.payload.VehicleLocationMessage;
import com.logistics.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleService.getVehicleById(id);
        return vehicle.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        return ResponseEntity.ok(createdVehicle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicle);
        return ResponseEntity.ok(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/faults")
    public ResponseEntity<List<Fault>> getFaultsByVehicleId(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getFaultsByVehicleId(id));
    }

    // Endpoint to update vehicle location
    @PutMapping("/{vehicleId}/location")
    public ResponseEntity<String> updateVehicleLocation(
            @PathVariable Long vehicleId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {

        vehicleService.updateVehicleLocation(vehicleId, latitude, longitude);
        return ResponseEntity.ok("Vehicle location updated successfully");
    }

    // Endpoint to get vehicle location
    @GetMapping("/{vehicleId}/location")
    public ResponseEntity<Map<String, Double>> getVehicleLocation(@PathVariable Long vehicleId) {
        Vehicle vehicle = vehicleService.findById(vehicleId);

        Map<String, Double> location = new HashMap<>();
        location.put("latitude", vehicle.getLatitude());
        location.put("longitude", vehicle.getLongitude());

        return ResponseEntity.ok(location);
    }

    @MessageMapping("/vehicle/location")
    @SendTo("/topic/vehicleLocation")
    public VehicleLocationMessage sendLocationUpdate(VehicleLocationMessage message) {
        System.out.println("Received location update: " + message);
        return message; // Broadcast the received message
    }

    @PutMapping("/{vehicleId}/assign-driver")
    public ResponseEntity<Vehicle> assignDriverToVehicle(
            @PathVariable Long vehicleId,
            @RequestBody Driver driver
    ) {
        Vehicle vehicle = vehicleService.assignDriverToVehicle(vehicleId, driver.getId());
        return ResponseEntity.ok(vehicle);
    }
}
