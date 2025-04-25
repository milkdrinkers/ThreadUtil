pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" // allow automatic download of JDKs
}

rootProject.name = "ThreadUtil"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "common",
    "bukkit",
    "folia",
    "velocity",
    "sponge:sponge7",
    "sponge:sponge8",
    "sponge:sponge9",
    "sponge:sponge10",
    "sponge:sponge11",
    "sponge:sponge12",
    "sponge:sponge13",
)