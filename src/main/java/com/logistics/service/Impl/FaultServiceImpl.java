package com.logistics.service.Impl;

import com.logistics.entity.Driver;
import com.logistics.entity.Fault;
import com.logistics.entity.Vehicle;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.FaultRepository;
import com.logistics.repository.VehicleRepository;
import com.logistics.service.FaultService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FaultServiceImpl implements FaultService {

    private final FaultRepository faultRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public FaultServiceImpl(FaultRepository faultRepository,
                            DriverRepository driverRepository,
                            VehicleRepository vehicleRepository) {
        this.faultRepository = faultRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Fault createFault(Fault fault) {
        attachRelations(fault);
        return faultRepository.save(fault);
    }

    @Override
    public Fault updateFault(Long id, Fault updatedFault) {
        Optional<Fault> existingFault = faultRepository.findById(id);
        if (existingFault.isPresent()) {
            Fault fault = existingFault.get();
            fault.setDescription(updatedFault.getDescription());
            fault.setReportedAt(updatedFault.getReportedAt());
            fault.setResolved(updatedFault.isResolved());
            fault.setResolutionNotes(updatedFault.getResolutionNotes());
            if (updatedFault.getDriver() != null || updatedFault.getVehicle() != null) {
                attachRelations(updatedFault);
                if (updatedFault.getDriver() != null) {
                    fault.setDriver(updatedFault.getDriver());
                }
                if (updatedFault.getVehicle() != null) {
                    fault.setVehicle(updatedFault.getVehicle());
                }
            }
            return faultRepository.save(fault);
        }
        throw new RuntimeException("Fault not found with id: " + id);
    }

    @Override
    public Fault markAsResolved(Long id, String resolutionNotes) {
        Optional<Fault> existingFault = faultRepository.findById(id);
        if (existingFault.isPresent()) {
            Fault fault = existingFault.get();
            fault.setResolved(true);
            fault.setResolutionNotes(resolutionNotes);
            return faultRepository.save(fault);
        }
        throw new RuntimeException("Fault not found with id: " + id);
    }

    @Override
    public List<Fault> getAllFaults() {
        return faultRepository.findAll();
    }

    @Override
    public Fault getFaultById(Long id) {
        return faultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fault not found with id: " + id));
    }

    @Override
    public List<Fault> getFaultsByResolvedStatus(boolean resolved) {
        return faultRepository.findByResolved(resolved);
    }

    @Override
    public void deleteFault(Long id) {
        faultRepository.deleteById(id);
    }

    private void attachRelations(Fault fault) {
        if (fault.getDriver() != null && fault.getDriver().getId() != null) {
            Driver driver = driverRepository.findById(fault.getDriver().getId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with id: " + fault.getDriver().getId()));
            fault.setDriver(driver);
        }
        if (fault.getVehicle() != null && fault.getVehicle().getId() != null) {
            Vehicle vehicle = vehicleRepository.findById(fault.getVehicle().getId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + fault.getVehicle().getId()));
            fault.setVehicle(vehicle);
        }
    }
}
