package com.logistics.DTO;

import com.logistics.entity.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    private final DriverMapper driverMapper;

    @Autowired
    public VehicleMapper(DriverMapper driverMapper) {
        this.driverMapper = driverMapper;
    }

    public VehicleDTO toDTO(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleDTO dto = new VehicleDTO();
        dto.setId(vehicle.getId());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setColor(vehicle.getColor());
        dto.setActive(vehicle.isActive());
        dto.setLastServiceDate(vehicle.getLastServiceDate());
        dto.setLatitude(vehicle.getLatitude());
        dto.setLongitude(vehicle.getLongitude());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setVehicleTypeDefaulted(vehicle.isVehicleTypeDefaulted());

        if (vehicle.getDriver() != null) {
            dto.setDriver(driverMapper.toDTO(vehicle.getDriver()));
        }

        return dto;
    }

    public Vehicle toEntity(VehicleDTO dto) {
        if (dto == null) {
            return null;
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setId(dto.getId());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setMake(dto.getMake());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        vehicle.setColor(dto.getColor());
        vehicle.setActive(dto.isActive());
        vehicle.setLastServiceDate(dto.getLastServiceDate());
        vehicle.setLatitude(dto.getLatitude());
        vehicle.setLongitude(dto.getLongitude());
        vehicle.setVehicleType(dto.getVehicleType());

        if (dto.getDriver() != null) {
            vehicle.setDriver(driverMapper.toEntity(dto.getDriver()));
        }

        return vehicle;
    }
}