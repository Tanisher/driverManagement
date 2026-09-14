package com.logistics.service.Impl;

import com.logistics.DTO.DriverMapper;
import com.logistics.entity.Driver;
import com.logistics.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    void expiringLicensesQueryUsesTodayThroughPlus30Days() {
        driverService.getDriversWithLicensesExpiringWithinDays(30);

        LocalDate from = LocalDate.now();
        verify(driverRepository).findByLicenseExpiryDateBetween(from, from.plusDays(30));
    }
}
