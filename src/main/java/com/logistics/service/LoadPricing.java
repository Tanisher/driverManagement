package com.logistics.service;

import com.logistics.entity.CargoType;
import com.logistics.entity.Load;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mutually exclusive load pricing: PER_KM uses ratePerKm × loadedKm × tonnage; FLAT uses flatAmount only.
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

    public static BigDecimal billableAmount(Load load, LoadedTrip loadedTrip) {
        if (load == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Load is required");
        }
        validate(load.getPricingMode(), load.getRatePerKm(), load.getFlatAmount());
        requireCompletedLoadedTrip(loadedTrip);
        if (load.getPricingMode() == PricingMode.FLAT) {
            return load.getFlatAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal loadedKm = loadedDistanceKm(loadedTrip);
        return load.getRatePerKm()
                .multiply(loadedKm)
                .multiply(tonnage(load))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * BAGGED (and other non-BULK): stated {@code weight} only.
     * BULK: weighbridge {@code actualWeight} when set, otherwise stated {@code weight}.
     */
    public static BigDecimal tonnage(Load load) {
        if (load.getCargoType() == CargoType.BULK && load.getActualWeight() != null) {
            return load.getActualWeight();
        }
        return parseStatedWeight(load.getWeight());
    }

    /**
     * True when a BULK PER_KM bill used stated weight because no weighbridge reading exists.
     */
    public static boolean isWeightEstimated(Load load) {
        return load != null
                && load.getPricingMode() == PricingMode.PER_KM
                && load.getCargoType() == CargoType.BULK
                && load.getActualWeight() == null;
    }

    public static BigDecimal loadedDistanceKm(LoadedTrip loadedTrip) {
        requireCompletedLoadedTrip(loadedTrip);
        return BigDecimal.valueOf(loadedTrip.getEndMileage())
                .subtract(BigDecimal.valueOf(loadedTrip.getStartMileage()));
    }

    private static BigDecimal parseStatedWeight(String weight) {
        if (weight == null || weight.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Load weight is required for PER_KM billing");
        }
        try {
            return new BigDecimal(weight.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Load weight must be a number");
        }
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
