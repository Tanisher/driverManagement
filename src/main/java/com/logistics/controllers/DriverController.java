package com.logistics.controllers;

import com.logistics.DTO.DriverDTO;
import com.logistics.entity.Driver;
import com.logistics.DTO.DriverMapper;
import com.logistics.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;
    private final DriverMapper driverMapper;

    @Autowired
    public DriverController(DriverService driverService, DriverMapper driverMapper) {
        this.driverService = driverService;
        this.driverMapper = driverMapper;
    }

    // Add error handling for driver creation
    @PostMapping
    public ResponseEntity<DriverDTO> createDriver(@RequestBody DriverDTO driverDTO) {
        try {
            Driver driver = driverMapper.toEntity(driverDTO);
            Driver savedDriver = driverService.saveDriver(driver);
            return ResponseEntity.ok(driverMapper.toDTO(savedDriver));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverDTO> updateDriver(@PathVariable Long id, @RequestBody DriverDTO driverDTO) {
        Driver updated = driverService.updateDriver(id, driverDTO);
        return ResponseEntity.ok(driverMapper.toDTO(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DriverDTO> patchDriver(@PathVariable Long id, @RequestBody DriverDTO driverDTO) {
        Driver updated = driverService.patchDriver(id, driverDTO);
        return ResponseEntity.ok(driverMapper.toDTO(updated));
    }

    @GetMapping("/expiring-licenses")
    public ResponseEntity<List<DriverDTO>> getDriversWithExpiringLicenses() {
        List<DriverDTO> driverDTOs = driverService.getDriversWithLicensesExpiringWithinDays(30)
                .stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(driverDTOs);
    }

    @GetMapping
    public ResponseEntity<List<DriverDTO>> getAllDrivers() {
        List<DriverDTO> driverDTOs = driverService.getAllDrivers()
                .stream()
                .map(driverMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(driverDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable Long id) {
        Driver driver = driverService.getDriverById(id);
        return ResponseEntity.ok(driverMapper.toDTO(driver));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}