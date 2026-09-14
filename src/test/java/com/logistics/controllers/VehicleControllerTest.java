package com.logistics.controllers;

import com.logistics.DTO.FaultMapper;
import com.logistics.DTO.VehicleMapper;
import com.logistics.payload.VehicleLocationMessage;
import com.logistics.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock private VehicleService vehicleService;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private FaultMapper faultMapper;

    @InjectMocks
    private VehicleController vehicleController;

    @Test
    void stompLocationHandlerPersistsThroughVehicleServiceBeforeBroadcast() {
        VehicleLocationMessage message = new VehicleLocationMessage();
        message.setVehicleId(5L);
        message.setLatitude(-17.825);
        message.setLongitude(31.033);

        VehicleLocationMessage broadcast = vehicleController.sendLocationUpdate(message);

        verify(vehicleService).updateVehicleLocation(5L, -17.825, 31.033);
        assertSame(message, broadcast);
    }
}
