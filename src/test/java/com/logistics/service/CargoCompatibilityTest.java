package com.logistics.service;

import com.logistics.entity.CargoType;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CargoCompatibilityTest {

    @Test
    void matchingTypesAreAllowed() {
        assertDoesNotThrow(() -> CargoCompatibility.assertVehicleCanCarry(CargoType.BAGGED, CargoType.BAGGED));
        assertDoesNotThrow(() -> CargoCompatibility.assertVehicleCanCarry(CargoType.BULK, CargoType.BULK));
        assertDoesNotThrow(() -> CargoCompatibility.assertVehicleCanCarry(CargoType.GENERAL, CargoType.GENERAL));
    }

    @Test
    void mismatchIsHardBlockWithNamedTypes() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> CargoCompatibility.assertVehicleCanCarry(CargoType.BULK, CargoType.BAGGED));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Vehicle type BULK cannot carry cargo type BAGGED", ex.getReason());
    }
}
