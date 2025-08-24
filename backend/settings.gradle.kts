plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  versionCatalogs { create("cs") { from("io.github.truenine:composeserver-version-catalog:0.0.30") } }
}

rootProject.name = "backend"
