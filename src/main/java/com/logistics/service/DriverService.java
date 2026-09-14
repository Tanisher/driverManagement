package com.logistics.service;

import com.logistics.DTO.DriverDTO;
import com.logistics.entity.Driver;

import java.util.List;

public interface DriverService {
    Driver saveDriver(Driver driver);
    List<Driver> getAllDrivers();
    Driver getDriverById(Long id);
    Driver updateDriver(Long id, DriverDTO driverDTO);
    Driver patchDriver(Long id, DriverDTO driverDTO);
    void deleteDriver(Long id);
    List<Driver> getDriversWithLicensesExpiringWithinDays(int days);
}