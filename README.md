# Socotra Solutions Engineer Take Home Project

## Getting Started

Download the community edition of IntelliJ

Familiarize yourself with the [Socotra Insurance Suite Documentation](https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/gettingStarted/introduction-to-socotra.html) and [Socotra UI](https://ui-ec-sandbox.socotra.com)

## Set-up

### Documentation:
* [Socotra Configuration SDK](https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/configuration/generalTopics/configurationSdk.html)


### Steps:
1. Set `SOCOTRA_KERNEL_ACCESS_TOKEN` in your environment (or hard-code into `build.gradle.kts`). You can generate one via the api or admin UI
2. Run `./gradlew createTenant` to launch a Socotra tenant with the config. You will need the tenant locator it returns to configure Postman, as the `tenantLocator` variable in the **SimpleAuto** Environment.
3. Run `./gradlew refreshReferenceDataModel` and `./gradlew --refresh-dependencies` to ensure the your dev environment has the necessary dependencies and generated classes.
4. Make changes as needed, running `./gradlew deployConfigToTenant` when ready to test.
    * Backwards-incompatible changes will require `./gradlew overwriteConfigToTenant`
5. You can test changes using the provided Postman collection or [Socotra Operations UI](https://ui-ec-sandbox.socotra.com/em/operations)


## SimpleAuto Rating Plugin

This simplified personal auto insurance product is configured so a quote contains a list of Drivers, as well as a list of Vehicles, each vehicle having four possible coverages attached to it. To calculate a rate for the quote, each coverage on each vehicle is rated independently based on factors looked up from the drivers, from the vehicle, and from the coverages themselves.

There are two mandatory coverages (BodilyInjury and PropertyDamage), and two optional (Collision and Comprehensive).

Each coverage is calculated as a BaseRate multiplied by a set of factors. The BaseRate is looked up by vehicle type, and factors determined by Driver Age, and each Deductible or Limit found on the coverage.
ex: `rateBodilyInjury()` where `vehicleType`=Car, `Driver.age`=24, `Coverage.limit`=$50,000

```
  500.00  (BaseRate)
*   1.75  (DriverAgeFactor)
*   1.10  (LimitFactor)
------------------------
= 962.50
```

See the include `SimpleAutoRater.xlsx`in the resources folder Excel rater for the full rate calc formulas.

The rating algorithm code is partially implemented in `socotra-config/plugins/java/SimpleAutoRatingPlugin.java`, with a few remaining tasks to complete.


## Take Home Project Tasks

1. Implement `rateVehicles` to iteratively rate each Vehicle and return all `RatingItem`s
2. Implement rating for the Comprehensive coverage.
3. Implement `lookupHighestDriverAgeFactor` to determine the age factor that is highest on all Drivers on the policy
4. Implement a new UninsuredMotorist coverage type, that has both a `Limit` and `Deductible` (you can use the same factor lookups as other coverages).


## Socotra Config Components

### Documentation:
* Config: https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/gettingStarted/create-a-tenant-configuration-file.html
* Plugins: https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/configuration/plugins/overview.html


### Config Components
* `socotra-config/config.json` is for global settings and defaults
    * `products/SimpleAuto` is the quote-level data and top-level elements
    * `exposures` contains configuration for `Drivers` and `Vehicles`
    * `coverages` contains config for per-Vehicle coverages (`BodilyInjury`, `Collision`, `Comprehensive`, `PropertyDamage`)
    * `coverageTerms` contains config for per-coverage `Limit`s and `Deductible`s

### Code Components
* `plugins/java` contains the implementation of the Rating Plugin
* Notes:
    * The Socotra plugin platform does not allow 3rd-party library dependencies to be bundled in the plugin configuration, but you can create additional utility classes if needed. Everything will always be packaged under `com.socotra.deployment.customer`.
    * Feel free to refactor / improve / simplify / reimplement any of the `private` methods in `SimpleAutoRatingPlugin` as needed.


## Resources

### Documentation
*  [Socotra Insurance Suite Documentation](https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/gettingStarted/introduction-to-socotra.html)
    * [Configuration](https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/gettingStarted/create-a-tenant-configuration-file.html)
    * [Plugins Overview](https://socotra-generated-documentation.s3-eu-west-1.amazonaws.com/sphinx/ec/configuration/plugins/overview.html)

### Other
* Postman Resources - in `resources/SimpleAuto.postman_collection.json` and `SimpleAuto.postman_environment.json`
    * This collection has basic examples of creating Accounts, Quotes, validation and rating with the SimpleAuto product.
    * Your `tenantLocator` and `personalAccessToken` will need to be configured in the environment.
    * [Postman Documentation](https://learning.postman.com/docs/introduction/overview/)
* Excel Rater - in `resources/SimpleAutoRater.xlsx`
    * This spreadsheet implements a simplified (single-Driver) version of the rating algorithm used here and can be used to compare against.
