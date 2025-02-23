plugins {
    java
    id("socotra-ec-config-developer") version "v0.6.5"
}

`socotra-config-developer` {
    // REMOVE REMOVE REMOVE
//    apiUrl.set("https://api-kernel-dev.socotra.com")
//    tenantLocator.set("47a63b3c-026b-4326-9ae3-4c7509c8f887")
//    personalAccessToken.set("SOCP_01JJP55W2DSGEAW3BHSMB5M0WH")

     apiUrl.set("https://api-ec-sandbox.socotra.com")
     tenantLocator.set(System.getenv("SOCOTRA_KERNEL_TENANT_LOCATOR") ?: "hardcoded-fallback-tenant-locator")
     personalAccessToken.set(System.getenv("SOCOTRA_KERNEL_ACCESS_TOKEN") ?: "hardcoded-fallback-access-token")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}
