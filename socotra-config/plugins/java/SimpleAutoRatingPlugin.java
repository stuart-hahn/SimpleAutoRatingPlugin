package com.socotra.deployment.customer;

import com.socotra.coremodel.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
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



    // =============================
    // VEHICLE RATING METHODS
    // =============================

    /**
     * Calculates rates for all vehicles within the given policy.
     *
     * @param policy The auto policy containing vehicles.
     * @return A list of RatingItem objects for each vehicle's coverages.
     */
    private List<RatingItem> rateVehicles(SimpleAuto policy) {
        logger.info("rateVehicles vehicles={}", policy.vehicles());

        // List to hold all rating items from all vehicles
        List<RatingItem> allVehicleRates = new ArrayList<>();

        // Iterate over each vehicle and accumulate its coverage ratings
        for (Vehicle vehicle : policy.vehicles()) {
            allVehicleRates.addAll(rateVehicleCoverages(vehicle, policy));
        }

        logger.info("rateVehicles rates={}", allVehicleRates);
        return allVehicleRates;
    }

    /**
     * Calculates rates for each coverage available on a vehicle.
     *
     * @param vehicle The vehicle whose coverages are being rated.
     * @param policy  The policy context (includes drivers and other details).
     * @return A list of RatingItem objects representing the vehicle's coverages.
     */
    private List<RatingItem> rateVehicleCoverages(Vehicle vehicle, SimpleAuto policy) {
        logger.info("rateVehicleCoverages vehicle={}", vehicle);

        // List to hold rating items for each coverage on the vehicle
        List<RatingItem> coverageRates = new ArrayList<>();

        // Required Coverages: Bodily Injury and Property Damage
        coverageRates.add(this.rateBodilyInjury(vehicle, vehicle.bodilyInjury(), policy));
        coverageRates.add(this.ratePropertyDamage(vehicle, vehicle.propertyDamage(), policy));

        // Optional Coverage: Collision
        if (vehicle.collision() != null) {
            coverageRates.add(this.rateCollision(vehicle, vehicle.collision(), policy));
        }

        // Optional Coverage: Comprehensive
        if (vehicle.comprehensive() != null) {
            coverageRates.add(this.rateComprehensive(vehicle, vehicle.comprehensive(), policy));
        }

        // Optional Coverage: Uninsured Motorist
        if (vehicle.uninsuredMotorist() != null) {
            coverageRates.add(this.rateUninsuredMotorist(vehicle, vehicle.uninsuredMotorist(), policy));
        }

        logger.info("rateVehicleCoverages rates={}", coverageRates);
        return coverageRates;
    }


    // =============================
    // COVERAGE RATING METHODS
    // =============================

/**
 * COVERAGE RATING:
 *
 * Each coverage looks up a BaseRate by vehicle type, then multiplies it by a number
 * of factors: HighestDriverFactor determined by driver age, and limit and deductible factors
 * determined by the coverageTerms available on that particular coverage.
 */

    /**
     * Calculates rate for Bodily Injury coverage.
     * Formula: BaseRate * DriverAgeFactor * LimitFactor
     *
     * @param vehicle  The vehicle being rated.
     * @param coverage The Bodily Injury coverage details.
     * @param policy   The policy context (driver details, etc.).
     * @return A RatingItem for the Bodily Injury coverage.
     */
    private RatingItem rateBodilyInjury(Vehicle vehicle, BodilyInjury coverage, SimpleAuto policy) {
        logger.info("rateBodilyInjury coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor(policy)
            * this.lookupLimitFactor(coverage.limit());

        rate /= 12;

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculates rate for Property Damage coverage.
     * Formula: BaseRate * DriverAgeFactor * LimitFactor
     *
     * @param vehicle  The vehicle being rated.
     * @param coverage The Property Damage coverage details.
     * @param policy   The policy context (driver details, etc.).
     * @return A RatingItem for the Property Damage coverage.
     */
    private RatingItem ratePropertyDamage(Vehicle vehicle, PropertyDamage coverage, SimpleAuto policy) {
        logger.info("ratePropertyDamage coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor(policy)
            * this.lookupLimitFactor(coverage.limit());

        rate /= 12;

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculates rate for Collision coverage.
     * Formula: BaseRate * DriverAgeFactor * DeductibleFactor
     *
     * @param vehicle  The vehicle being rated.
     * @param coverage The Collision coverage details.
     * @param policy   The policy context (driver details, etc.).
     * @return A RatingItem for the Collision coverage.
     */
    private RatingItem rateCollision(Vehicle vehicle, Collision coverage, SimpleAuto policy) {
        logger.info("rateCollision coverage={}", coverage);

        double rate = this.lookupBaseRate(vehicle)
            * this.lookupHighestDriverAgeFactor(policy)
            * this.lookupDeductibleFactor(coverage.deductible());

        rate /= 12;

        RatingItem ratingItem = RatingItem.builder()
            .elementLocator(coverage.locator())
            .chargeType(ChargeType.premium)
            .rate(new BigDecimal(rate))
            .build();

        logger.info("ratingItem={}", ratingItem);
        return ratingItem;
    }

    /**
     * Calculates rate for Comprehensive coverage.
     * Formula: BaseRate * DriverAgeFactor * DeductibleFactor
     *
     * @param vehicle  The vehicle being rated.
     * @param coverage The Comprehensive coverage details.
     * @param policy   The policy context (driver details, etc.).
     * @return A RatingItem for the Comprehensive coverage, or null if coverage is null.
     */
    private RatingItem rateComprehensive(Vehicle vehicle, Comprehensive coverage, SimpleAuto policy) {
        // If the coverage is null, log a warning and return null.
        if (coverage == null) {
            logger.warn("rateComprehensive called with null coverage for vehicle: {}", vehicle);
            return null;
        }

        logger.info("Calculating Comprehensive rate for vehicle={} with coverage={}", vehicle, coverage);

        double baseRate = this.lookupBaseRate(vehicle);
        double driverAgeFactor = this.lookupHighestDriverAgeFactor(policy);
        double deductibleFactor = this.lookupDeductibleFactor(coverage.deductible());

        double rate = baseRate * driverAgeFactor * deductibleFactor;

        rate /= 12;

        RatingItem ratingItem = RatingItem.builder()
                .elementLocator(coverage.locator())
                .chargeType(ChargeType.premium)
                .rate(new BigDecimal(rate))
                .build();

        return ratingItem;
    }

    /**
     * Calculates rate for Uninsured Motorist coverage.
     * Formula: BaseRate * DriverAgeFactor * LimitFactor * DeductibleFactor
     *
     * @param vehicle  The vehicle being rated.
     * @param coverage The Uninsured Motorist coverage details.
     * @param policy   The policy context (driver details, etc.).
     * @return A RatingItem for the Uninsured Motorist coverage, or null if coverage is null.
     */
    private RatingItem rateUninsuredMotorist(Vehicle vehicle, UninsuredMotorist coverage, SimpleAuto policy) {
        // If the coverage is null, log a warning and return null.
        if (coverage == null) {
            logger.warn("rateUninsuredMotorist called with null coverage for vehicle: {}", vehicle);
            return null;
        }

        logger.info("Calculating UninsuredMotorist rate for vehicle={} with coverage={}", vehicle, coverage);

        double baseRate = this.lookupBaseRate(vehicle);
        double driverAgeFactor = this.lookupHighestDriverAgeFactor(policy);
        double limitFactor = this.lookupLimitFactor(coverage.limit());
        double deductibleFactor = this.lookupDeductibleFactor(coverage.deductible());

        double rate = baseRate * driverAgeFactor * limitFactor * deductibleFactor;

        rate /= 12;

        RatingItem ratingItem = RatingItem.builder()
                .elementLocator(coverage.locator())
                .chargeType(ChargeType.premium)
                .rate(new BigDecimal(rate))
                .build();

        logger.info("Generated UninsuredMotorist RatingItem: {}", ratingItem);
        return ratingItem;
    }


    // =============================
    // LOOKUP TABLE METHODS
    // =============================

    /**
     * Looks up the base rate for a vehicle based on its type.
     *
     * @param vehicle The vehicle to lookup.
     * @return The base rate as a double.
     */
    private double lookupBaseRate(Vehicle vehicle) {
        double baseRate = switch (vehicle.data().vehicleType()) {
            case "Motorcycle" -> 300.0;
            case "Car" -> 500.0;
            case "Truck" -> 750.0;
            default -> throw new RuntimeException("lookupBaseRate failed!");
        };
        logger.info("lookupBaseRate baseRate={}", baseRate);
        return baseRate;
    }

    /**
     * Looks up the deductible factor for a given deductible value.
     *
     * @param deductible The deductible from the coverage.
     * @return The corresponding deductible factor.
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
     * Looks up the limit factor for a given coverage limit.
     *
     * @param limit The coverage limit.
     * @return The corresponding limit factor.
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
     * Determines the highest driver age factor among all drivers on the policy.
     * The highest factor is used to adjust the rate.
     *
     * Age to Factor Mapping:
     *   - Age <= 20  => 2.50
     *   - Age 21-25  => 1.75
     *   - Age 26-40  => 1.10
     *   - Age 41-60  => 0.75
     *   - Age > 60   => 0.50
     *
     * @param policy The auto policy containing drivers.
     * @return The highest driver age factor.
     */
    private double lookupHighestDriverAgeFactor(SimpleAuto policy) {
        double highestDriverAgeFactor = 2.5;

        // Loop through all drivers to find the maximum age factor
        for (Driver driver : policy.drivers()) {
            int age = calculateAge(driver);
            double factor = getDriverAgeFactor(age);
            highestDriverAgeFactor = Math.min(highestDriverAgeFactor, factor);
        }

        return highestDriverAgeFactor;
    }

    /**
     * Calculates the age of a driver based on their date of birth.
     *
     * @param driver The driver whose age is to be calculated.
     * @return The age in years.
     */
    private int calculateAge(Driver driver) {
        return Period.between(driver.data().dateOfBirth(), LocalDate.now()).getYears();
    }

    /**
     * Returns the driver age factor based on the driver's age.
     *
     * @param age The age of the driver.
     * @return The corresponding age factor.
     */
    private double getDriverAgeFactor(int age) {
        if (age <= 20) {
            return 2.5;
        } else if (age <= 25) {
            return 1.75;
        } else if (age <= 40) {
            return 1.10;
        } else if (age <= 60) {
            return 0.75;
        } else {
            return 0.5;
        }
    }
}