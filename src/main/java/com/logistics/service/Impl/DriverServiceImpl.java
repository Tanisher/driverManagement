package com.logistics.service.Impl;


import com.logistics.DTO.DriverDTO;
import com.logistics.DTO.DriverMapper;
import com.logistics.entity.Driver;
import com.logistics.repository.DriverRepository;
import com.logistics.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final PasswordEncoder passwordEncoder;

    public DriverServiceImpl(DriverRepository driverRepository, DriverMapper driverMapper) {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Driver saveDriver(Driver driver) {
        if (driver.getPassword() != null && !driver.getPassword().isBlank()) {
            driver.setPassword(passwordEncoder.encode(driver.getPassword()));
        }
        return driverRepository.save(driver);
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }

    @Override
    public Driver updateDriver(Long id, DriverDTO driverDTO) {
        Driver existing = getDriverById(id);
        driverMapper.apply(driverDTO, existing);
        return driverRepository.save(existing);
    }

    @Override
    public Driver patchDriver(Long id, DriverDTO driverDTO) {
        Driver existing = getDriverById(id);
        driverMapper.applyNonNull(driverDTO, existing);
        return driverRepository.save(existing);
    }

    @Override
    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }

    @Override
    public List<Driver> getDriversWithLicensesExpiringWithinDays(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return driverRepository.findByLicenseExpiryDateBetween(from, to);
    }
}
