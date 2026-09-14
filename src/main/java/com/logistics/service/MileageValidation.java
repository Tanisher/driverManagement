package com.logistics.service;

import com.logistics.exception.InvalidMileageException;

/**
 * Shared odometer checks for deadhead start, trip end, and office trip create.
 */
public final class MileageValidation {

    private MileageValidation() {
    }

    public static void assertNonNegative(Double mileage, String fieldName) {
        if (mileage == null) {
            throw new InvalidMileageException(fieldName + " is required");
        }
        if (mileage < 0) {
            throw new InvalidMileageException(fieldName + " must not be negative");
        }
    }

    public static void assertEndNotBelowStart(Double startMileage, Double endMileage) {
        assertNonNegative(endMileage, "endMileage");
        if (startMileage != null && endMileage < startMileage) {
            throw new InvalidMileageException("endMileage must not be lower than startMileage");
        }
    }

    public static void assertNotBelowLastRecorded(Double startMileage, Double lastEndMileage) {
        if (lastEndMileage != null && startMileage < lastEndMileage) {
            throw new InvalidMileageException(
                    "startMileage must not be lower than the vehicle's last recorded end mileage ("
                            + lastEndMileage + ")");
        }
    }
}
