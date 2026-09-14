package com.logistics.service.Impl;

import com.logistics.entity.*;
import com.logistics.payload.SignupRequest;
import com.logistics.repository.*;
import com.logistics.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final MechanicRepository mechanicRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, DriverRepository driverRepository, AdminRepository adminRepository, OfficeStaffRepository officeStaffRepository
    , MechanicRepository mechanicRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.driverRepository = driverRepository;
        this.adminRepository = adminRepository;
        this.officeStaffRepository = officeStaffRepository;
        this.mechanicRepository = mechanicRepository;
    }

    @Override
    public User registerUser(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User.UserRole role = request.getRole() != null ? request.getRole() : User.UserRole.OFFICE;

        switch (role) {
            case DRIVER:
                Driver driver = new Driver();
                applyUserFields(driver, request, encodedPassword, User.UserRole.DRIVER);
                driver.setNationalId(request.getNationalId());
                driver.setLicenseExpiryDate(request.getLicenseExpiryDate());
                if (driver.getName() == null) {
                    driver.setName("Default Name");
                }
                if (driver.getLicenseNumber() == null) {
                    driver.setLicenseNumber("Default License");
                }
                return driverRepository.save(driver);

            case ADMIN:
                Admin admin = new Admin();
                applyUserFields(admin, request, encodedPassword, User.UserRole.ADMIN);
                return adminRepository.save(admin);

            case MECHANIC:
                Mechanic mechanic = new Mechanic();
                applyUserFields(mechanic, request, encodedPassword, User.UserRole.MECHANIC);
                return mechanicRepository.save(mechanic);

            case OFFICE:
            default:
                OfficeStaff officeStaff = new OfficeStaff();
                applyUserFields(officeStaff, request, encodedPassword, User.UserRole.OFFICE);
                return officeStaffRepository.save(officeStaff);
        }
    }

    private void applyUserFields(User user, SignupRequest request, String encodedPassword, User.UserRole role) {
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(request.getEmail());
        user.setRole(role);
    }
}
