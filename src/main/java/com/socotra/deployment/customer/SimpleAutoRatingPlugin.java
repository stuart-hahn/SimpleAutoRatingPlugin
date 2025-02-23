package com.socotra.deployment.customer;

import com.socotra.coremodel.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleAutoRatingPlugin implements RatePlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());

/**
 * REQUEST RATING:
 * rate(request) will be called by the platform when Price Quote/Transaction is called
 */
    /**
     * Plugin entrypoint to rate Quote Requests
     */
    @Override
    public RatingSet rate(SimpleAutoQuoteRequest request) {
        logger.info("rate({}) request={}", request.getClass(), request);

        SimpleAutoQuote quote = request.quote();
        logger.info("quote={}", quote);

        RatingSet ratingSet = RatingSet.builder()
            .ok(true)
            .addRatingItems(this.rateVehicles(quote))
            .build();

        logger.info("ratingSet={}", ratingSet);
        return ratingSet;
    }



/**
 * VEHICLE RATING:
 */
    /**
     * Calculates rates for all Vehicles for a Quote/Segment object
     */
    private List<RatingItem> rateVehicles(SimpleAuto policy) {
        logger.info("rateVehicles vehicles={}", policy.vehicles());

        // TODO: Implement applying rateVehicleCoverages() to each vehicle
        List<RatingItem> allVehicleRates = List.of();

        logger.info("rateVehicles rates={}", allVehicleRates);
        return allVehicleRates;
    }

    /**
     * Calculates rates for all of a Vehicle's coverages
     */
    private List<RatingItem> rateVehicleCoverages(Vehicle vehicle) {
        logger.info("rateVehicleCoverages vehicle={}", vehicle);

        List<RatingItem> coverageRates = new ArrayList<>();

        // Required Coverages:
        coverageRates.add(this.rateBodilyInjury(vehicle, vehicle.bodilyInjury()));
        coverageRates.add(this.ratePropertyDamage(vehicle, vehicle.propertyDamage()));

        // Optional Coverages:
        if (vehicle.collision() != null) {
            coverageRates.add(this.rateCollision(vehicle, vehicle.collision()));
        }
        // TODO: Implement rateComprehensive() when applicable

        logger.info("rateVehicleCoverages rates={}", coverageRates);
        return coverageRates;
    }



/**
 * COVERAGE RATING:
 *
 * Each coverage looks up a BaseRate by vehicle type, then multiplies it by a number
 * of factors: HighestDriverFactor determined by driver age, and limit and deductible factors
 * determined by the coverageTerms available on that particular coverage.
 */
    /** Calculate rate for BodilyInjury coverage as
     * = BaseRate * DriverAgeFactor * LimitFactor
     */
    private RatingItem rateBodilyInjury(Vehicle vehicle, BodilyInjury coverage) {
        logger.info("rateBodilyInjury coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor()
            * this.lookupLimitFactor(coverage.limit());

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculate rate for PropertyDamage coverage as
     * = BaseRate * DriverAgeFactor * LimitFactor
     */
    private RatingItem ratePropertyDamage(Vehicle vehicle, PropertyDamage coverage) {
        logger.info("ratePropertyDamage coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor()
            * this.lookupLimitFactor(coverage.limit());

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculate rate for Collision coverage as
     * = BaseRate * DriverAgeFactor * DeductibleFactor
     */
    private RatingItem rateCollision(Vehicle vehicle, Collision coverage) {
        logger.info("rateCollision coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor()
            * this.lookupDeductibleFactor(coverage.deductible());

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculate rate for Comprehensive coverage as
     * = BaseRate * DriverAgeFactor * DeductibleFactor
     */
    private RatingItem rateComprehensive() {
        // TODO: Implement rateComprehensive() with above formula
        throw new RuntimeException("Not implemented!");
    }



/**
 * LOOKUP TABLES:
 *
 * In-code implementation of simple lookups
 */
    /**
     * Lookup BaseRate for a Vehicle by type
     */
    private double lookupBaseRate(Vehicle vehicle) {
        double baseRate = switch (vehicle.data().vehicleType()) {
            case "Car" -> 500.0;
            case "Truck" -> 750.0;
            default -> throw new RuntimeException("lookupBaseRate failed!");
        };
        logger.info("lookupBaseRate baseRate={}", baseRate);
        return baseRate;
    }

    /**
     * Lookup DeductibleFactor by coverage deductible
     */
    private double lookupDeductibleFactor(Deductible deductible) {
        double factor = switch (deductible) {
            case DED_100 -> 1.50;
            case DED_250 -> 1.25;
            case DED_500 -> 0.95;
            case DED_1000 -> 0.75;
            case DED_2500 -> 0.50;
            default -> throw new RuntimeException("lookupDeductibleFactor failed!");
        };
        logger.info("lookupDeductibleFactor factor={}", factor);
        return factor;
    }

    /**
     * Lookup LimitFactor by coverage limit
     */
    private double lookupLimitFactor(Limit limit) {
        double factor = switch (limit) {
            case LIM_25 -> 0.8;
            case LIM_50 -> 1.1;
            case LIM_100 -> 2.0;
            case LIM_300 -> 5.0;
            default -> throw new RuntimeException("lookupLimitFactor failed!");
        };
        logger.info("lookupLimitFactor factor={}", factor);
        return factor;
    }

    /**
     * Lookup DriverAgeFactor by driver age
     */
    private double lookupHighestDriverAgeFactor() {
        // TODO: Implement lookup for driver age factor
        // and return the *highest* factor of any driver on the policy
        // ex: if a quote has DriverA=22 and DriverB=41, return 1.75

        // Age   => Factor
        // < 20  => 2.50
        // 21-25 => 1.75
        // 26-40 => 1.10
        // 41-60 => 0.75
        // 61+   => 0.50

        throw new RuntimeException("Not implemented!");
    }
}