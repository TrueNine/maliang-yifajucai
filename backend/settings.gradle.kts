plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  versionCatalogs { create("cs") { from("io.github.truenine:composeserver-version-catalog:0.0.26") } }
}

rootProject.name = "backend"

// 已重构为单模块项目，不再需要子模块
// include("app", "infrastructure", "domain")
