package com.logistics.service;

import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoadPricingTest {

    @Test
    void perKmRejectsMissingRate() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.validate(PricingMode.PER_KM, null, null));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("ratePerKm is required when pricingMode is PER_KM", ex.getReason());
    }

    @Test
    void perKmRejectsFlatAmount() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.validate(PricingMode.PER_KM, new BigDecimal("5.00"), new BigDecimal("1000")));
        assertEquals(400, ex.getStatusCode().value());
        assertEquals("flatAmount must not be set when pricingMode is PER_KM", ex.getReason());
    }

    @Test
    void flatRejectsMissingAmount() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.validate(PricingMode.FLAT, null, null));
        assertEquals("flatAmount is required when pricingMode is FLAT", ex.getReason());
    }

    @Test
    void flatRejectsRatePerKm() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.validate(PricingMode.FLAT, new BigDecimal("5.00"), new BigDecimal("1000")));
        assertEquals("ratePerKm must not be set when pricingMode is FLAT", ex.getReason());
    }

    @Test
    void perKmBillsLoadedKmOnly() {
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);
        BigDecimal amount = LoadPricing.billableAmount(
                PricingMode.PER_KM, new BigDecimal("4.50"), null, loaded);
        assertEquals(new BigDecimal("1125.00"), amount);
    }

    @Test
    void flatReturnsNegotiatedAmount() {
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);
        BigDecimal amount = LoadPricing.billableAmount(
                PricingMode.FLAT, null, new BigDecimal("800.5"), loaded);
        assertEquals(new BigDecimal("800.50"), amount);
    }

    @Test
    void incompleteLoadedTripIsRejected() {
        LoadedTrip loaded = new LoadedTrip();
        loaded.setStatus(TripStatus.ACTIVE);
        loaded.setStartMileage(1000.0);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.billableAmount(PricingMode.FLAT, null, new BigDecimal("800"), loaded));
        assertEquals("Loaded trip is not complete", ex.getReason());
    }

    private LoadedTrip completedLoadedTrip(double start, double end) {
        LoadedTrip loaded = new LoadedTrip();
        loaded.setStatus(TripStatus.COMPLETED);
        loaded.setStartMileage(start);
        loaded.setEndMileage(end);
        return loaded;
    }
}
