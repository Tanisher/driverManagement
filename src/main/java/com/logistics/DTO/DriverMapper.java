package com.logistics.DTO;

import com.logistics.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

    public DriverDTO toDTO(Driver driver) {
        if (driver == null) {
            return null;
        }

        DriverDTO dto = new DriverDTO();
        dto.setId(driver.getId());
        dto.setName(driver.getName());
        dto.setLastName(driver.getLastName());
        dto.setIdNumber(driver.getIdNumber());
        dto.setNationalId(driver.getNationalId());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setLicenseExpiryDate(driver.getLicenseExpiryDate());
        dto.setMobileNumber(driver.getMobileNumber());
        dto.setAddress(driver.getAddress());
        dto.setNextOfKin(driver.getNextOfKin());
        dto.setNextOfKinContact(driver.getNextOfKinContact());

        // Map User properties directly since Driver extends User
        UserDTO userDTO = new UserDTO();
        userDTO.setId(driver.getId());  // Same ID since it's inheritance
        userDTO.setUsername(driver.getUsername());
        userDTO.setEmail(driver.getEmail());
        userDTO.setRole(driver.getRole());
        dto.setUser(userDTO);

        return dto;
    }

    public void apply(DriverDTO dto, Driver driver) {
        driver.setName(dto.getName());
        driver.setLastName(dto.getLastName());
        driver.setIdNumber(dto.getIdNumber());
        driver.setNationalId(dto.getNationalId());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setMobileNumber(dto.getMobileNumber());
        driver.setAddress(dto.getAddress());
        driver.setNextOfKin(dto.getNextOfKin());
        driver.setNextOfKinContact(dto.getNextOfKinContact());
        applyUserFields(dto, driver, false);
    }

    public void applyNonNull(DriverDTO dto, Driver driver) {
        if (dto.getName() != null) {
            driver.setName(dto.getName());
        }
        if (dto.getLastName() != null) {
            driver.setLastName(dto.getLastName());
        }
        if (dto.getIdNumber() != null) {
            driver.setIdNumber(dto.getIdNumber());
        }
        if (dto.getNationalId() != null) {
            driver.setNationalId(dto.getNationalId());
        }
        if (dto.getLicenseNumber() != null) {
            driver.setLicenseNumber(dto.getLicenseNumber());
        }
        if (dto.getLicenseExpiryDate() != null) {
            driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        }
        if (dto.getMobileNumber() != null) {
            driver.setMobileNumber(dto.getMobileNumber());
        }
        if (dto.getAddress() != null) {
            driver.setAddress(dto.getAddress());
        }
        if (dto.getNextOfKin() != null) {
            driver.setNextOfKin(dto.getNextOfKin());
        }
        if (dto.getNextOfKinContact() != null) {
            driver.setNextOfKinContact(dto.getNextOfKinContact());
        }
        applyUserFields(dto, driver, true);
    }

    private void applyUserFields(DriverDTO dto, Driver driver, boolean partial) {
        if (dto.getUser() == null) {
            return;
        }
        if (!partial || dto.getUser().getUsername() != null) {
            driver.setUsername(dto.getUser().getUsername());
        }
        if (!partial || dto.getUser().getEmail() != null) {
            driver.setEmail(dto.getUser().getEmail());
        }
    }

    public Driver toEntity(DriverDTO dto) {
        if (dto == null) {
            return null;
        }

        Driver driver = new Driver();
        driver.setId(dto.getId());
        driver.setName(dto.getName());
        driver.setLastName(dto.getLastName());
        driver.setIdNumber(dto.getIdNumber());
        driver.setNationalId(dto.getNationalId());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setMobileNumber(dto.getMobileNumber());
        driver.setAddress(dto.getAddress());
        driver.setNextOfKin(dto.getNextOfKin());
        driver.setNextOfKinContact(dto.getNextOfKinContact());

        // Map User properties directly
        if (dto.getUser() != null) {
            driver.setUsername(dto.getUser().getUsername());
            driver.setEmail(dto.getUser().getEmail());
            driver.setRole(dto.getUser().getRole());
        }

        return driver;
    }
}