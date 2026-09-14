package com.logistics.service;

import com.logistics.entity.CargoType;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void baggedPerKmAlwaysUsesStatedWeight() {
        Load load = perKmLoad(CargoType.BAGGED, "2", new BigDecimal("99"));
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);

        BigDecimal amount = LoadPricing.billableAmount(load, loaded);

        // 4.50 × 250 km × 2 t (stated), ignores actualWeight 99
        assertEquals(new BigDecimal("2250.00"), amount);
        assertFalse(LoadPricing.isWeightEstimated(load));
        assertEquals(new BigDecimal("2"), LoadPricing.tonnage(load));
    }

    @Test
    void bulkPerKmUsesActualWeightWhenPresent() {
        Load load = perKmLoad(CargoType.BULK, "2", new BigDecimal("3"));
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);

        BigDecimal amount = LoadPricing.billableAmount(load, loaded);

        // 4.50 × 250 km × 3 t (weighbridge)
        assertEquals(new BigDecimal("3375.00"), amount);
        assertFalse(LoadPricing.isWeightEstimated(load));
        assertEquals(new BigDecimal("3"), LoadPricing.tonnage(load));
    }

    @Test
    void bulkPerKmFallsBackToStatedWeightWhenActualWeightNull() {
        Load load = perKmLoad(CargoType.BULK, "2", null);
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);

        BigDecimal amount = LoadPricing.billableAmount(load, loaded);

        // 4.50 × 250 km × 2 t (estimate)
        assertEquals(new BigDecimal("2250.00"), amount);
        assertTrue(LoadPricing.isWeightEstimated(load));
        assertEquals(new BigDecimal("2"), LoadPricing.tonnage(load));
    }

    @Test
    void flatReturnsNegotiatedAmount() {
        Load load = new Load();
        load.setPricingMode(PricingMode.FLAT);
        load.setFlatAmount(new BigDecimal("800.5"));
        LoadedTrip loaded = completedLoadedTrip(1000.0, 1250.0);

        BigDecimal amount = LoadPricing.billableAmount(load, loaded);
        assertEquals(new BigDecimal("800.50"), amount);
        assertFalse(LoadPricing.isWeightEstimated(load));
    }

    @Test
    void incompleteLoadedTripIsRejected() {
        Load load = new Load();
        load.setPricingMode(PricingMode.FLAT);
        load.setFlatAmount(new BigDecimal("800"));
        LoadedTrip loaded = new LoadedTrip();
        loaded.setStatus(TripStatus.ACTIVE);
        loaded.setStartMileage(1000.0);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> LoadPricing.billableAmount(load, loaded));
        assertEquals("Loaded trip is not complete", ex.getReason());
    }

    private Load perKmLoad(CargoType cargoType, String statedWeight, BigDecimal actualWeight) {
        Load load = new Load();
        load.setCargoType(cargoType);
        load.setWeight(statedWeight);
        load.setActualWeight(actualWeight);
        load.setPricingMode(PricingMode.PER_KM);
        load.setRatePerKm(new BigDecimal("4.50"));
        return load;
    }

    private LoadedTrip completedLoadedTrip(double start, double end) {
        LoadedTrip loaded = new LoadedTrip();
        loaded.setStatus(TripStatus.COMPLETED);
        loaded.setStartMileage(start);
        loaded.setEndMileage(end);
        return loaded;
    }
}
