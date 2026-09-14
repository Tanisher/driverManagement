package com.logistics.service;

import com.logistics.exception.InvalidMileageException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MileageValidationTest {

    @Test
    void rejectsNegativeMileage() {
        InvalidMileageException ex = assertThrows(
                InvalidMileageException.class,
                () -> MileageValidation.assertNonNegative(-1.0, "startMileage"));
        assertEquals("startMileage must not be negative", ex.getMessage());
    }

    @Test
    void allowsZeroMileage() {
        assertDoesNotThrow(() -> MileageValidation.assertNonNegative(0.0, "startMileage"));
    }

    @Test
    void rejectsStartBelowLastRecordedEnd() {
        InvalidMileageException ex = assertThrows(
                InvalidMileageException.class,
                () -> MileageValidation.assertNotBelowLastRecorded(1499.0, 1500.0));
        assertEquals(
                "startMileage must not be lower than the vehicle's last recorded end mileage (1500.0)",
                ex.getMessage());
    }

    @Test
    void allowsStartEqualToLastRecordedEnd() {
        assertDoesNotThrow(() -> MileageValidation.assertNotBelowLastRecorded(1500.0, 1500.0));
    }

    @Test
    void skipsLastRecordedCheckWhenVehicleHasNoPriorEnd() {
        assertDoesNotThrow(() -> MileageValidation.assertNotBelowLastRecorded(100.0, null));
    }

    @Test
    void rejectsEndBelowStart() {
        InvalidMileageException ex = assertThrows(
                InvalidMileageException.class,
                () -> MileageValidation.assertEndNotBelowStart(1000.0, 999.0));
        assertEquals("endMileage must not be lower than startMileage", ex.getMessage());
    }

    @Test
    void rejectsNegativeEndMileageBeforeComparingToStart() {
        InvalidMileageException ex = assertThrows(
                InvalidMileageException.class,
                () -> MileageValidation.assertEndNotBelowStart(1000.0, -1.0));
        assertEquals("endMileage must not be negative", ex.getMessage());
    }
}
