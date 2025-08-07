pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  versionCatalogs { create("cs") { from("io.github.truenine:composeserver-version-catalog:0.0.10") } }
}

rootProject.name = "tnmaster"

include("application", "infrastructure", "domain")
findProject(":application")?.name = "tnmaster-server"
