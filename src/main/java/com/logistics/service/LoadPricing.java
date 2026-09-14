package com.logistics.service;

import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mutually exclusive load pricing: PER_KM uses ratePerKm only; FLAT uses flatAmount only.
 * Billable loaded km never includes deadhead.
 */
public final class LoadPricing {

    private static final int MONEY_SCALE = 2;

    private LoadPricing() {
    }

    public static void validate(PricingMode pricingMode, BigDecimal ratePerKm, BigDecimal flatAmount) {
        if (pricingMode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pricingMode is required");
        }
        if (pricingMode == PricingMode.PER_KM) {
            if (ratePerKm == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ratePerKm is required when pricingMode is PER_KM");
            }
            if (flatAmount != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "flatAmount must not be set when pricingMode is PER_KM");
            }
            return;
        }
        if (flatAmount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "flatAmount is required when pricingMode is FLAT");
        }
        if (ratePerKm != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ratePerKm must not be set when pricingMode is FLAT");
        }
    }

    public static BigDecimal billableAmount(PricingMode pricingMode,
                                            BigDecimal ratePerKm,
                                            BigDecimal flatAmount,
                                            LoadedTrip loadedTrip) {
        validate(pricingMode, ratePerKm, flatAmount);
        requireCompletedLoadedTrip(loadedTrip);
        if (pricingMode == PricingMode.FLAT) {
            return flatAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal loadedKm = loadedDistanceKm(loadedTrip);
        return ratePerKm.multiply(loadedKm).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal loadedDistanceKm(LoadedTrip loadedTrip) {
        requireCompletedLoadedTrip(loadedTrip);
        return BigDecimal.valueOf(loadedTrip.getEndMileage())
                .subtract(BigDecimal.valueOf(loadedTrip.getStartMileage()));
    }

    private static void requireCompletedLoadedTrip(LoadedTrip loadedTrip) {
        if (loadedTrip == null
                || loadedTrip.getStatus() != TripStatus.COMPLETED
                || loadedTrip.getStartMileage() == null
                || loadedTrip.getEndMileage() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Loaded trip is not complete");
        }
    }
}
