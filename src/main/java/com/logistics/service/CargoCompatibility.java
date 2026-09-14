package com.logistics.service;

import com.logistics.entity.CargoType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Hard-block check: a driver's standing vehicle must match the load's cargo type.
 */
public final class CargoCompatibility {

    private CargoCompatibility() {
    }

    public static void assertVehicleCanCarry(CargoType vehicleType, CargoType cargoType) {
        if (cargoType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Load cargo type is not set");
        }
        if (vehicleType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle type is not set");
        }
        if (vehicleType != cargoType) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vehicle type " + vehicleType + " cannot carry cargo type " + cargoType);
        }
    }
}
