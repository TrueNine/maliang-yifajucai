import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  java
  alias(cs.plugins.io.github.truenine.composeserver.gradle.plugin)
  alias(cs.plugins.org.springframework.boot)
  alias(cs.plugins.org.jetbrains.kotlin.jvm)
  alias(cs.plugins.org.jetbrains.kotlin.plugin.spring)
  alias(cs.plugins.com.diffplug.spotless)
  alias(cs.plugins.com.google.devtools.ksp)
  alias(cs.plugins.io.spring.dependency.management)
  id("repositories-conventions")
}

group = "com.tnmaster"
version = "1.0"

composeGradle {
  jarExtension.enabled = true
  jarExtension {
    enabled = true
    bootJarClassifier = ""
    bootJarVersion = project.version.toString()
    bootJarSeparate = true
  }
}

sourceSets { main { resources { exclude("config/data/**") } } }

dependencies {
  implementation(platform(cs.org.springframework.boot.spring.boot.dependencies))
  implementation(platform(cs.org.springframework.boot.spring.boot.dependencies))
  implementation(platform(cs.org.springframework.cloud.spring.cloud.dependencies))
  implementation(platform(cs.org.springframework.modulith.spring.modulith.bom))

  implementation(cs.cn.dev33.sa.token.spring.boot3.starter)
  implementation(cs.cn.dev33.sa.token.redis.jackson)

  implementation(cs.org.springframework.boot.spring.boot.starter.data.redis)
  implementation(cs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
  implementation(cs.org.babyfish.jimmer.jimmer.spring.boot.starter)

  implementation(cs.com.yomahub.liteflow.spring.boot.starter) { exclude(group = "cn.hutool", module = "hutool-core") }

  implementation(cs.io.github.truenine.composeserver.cacheable)

  implementation(cs.io.github.truenine.composeserver.security.oauth2)
  implementation(cs.io.github.truenine.composeserver.security.crypto)
  implementation(cs.io.github.truenine.composeserver.rds.crud)
  implementation(cs.io.github.truenine.composeserver.rds.flyway.migration.postgresql)
  implementation(cs.io.github.truenine.composeserver.oss.minio)
  implementation(cs.io.github.truenine.composeserver.data.extract)
  implementation(cs.io.github.truenine.composeserver.data.crawler)
  implementation(cs.io.github.truenine.composeserver.depend.servlet)
  implementation(cs.org.springframework.boot.spring.boot.starter.validation)

  ksp(cs.org.babyfish.jimmer.jimmer.ksp)

  implementation(cs.io.github.truenine.composeserver.rds.jimmer.ext.postgres)

  implementation(cs.io.minio.minio)
  implementation(cs.cn.hutool.hutool.all)
  implementation(cs.io.github.truenine.composeserver.depend.jackson)

  runtimeOnly(cs.org.flywaydb.flyway.core)
  runtimeOnly(cs.org.flywaydb.flyway.database.postgresql)
  runtimeOnly(cs.org.postgresql.postgresql)

  testImplementation(cs.io.github.truenine.composeserver.testtoolkit)

  testImplementation(cs.org.testcontainers.testcontainers)
  testImplementation(cs.org.testcontainers.postgresql)
}

java {
  val jv = JavaVersion.VERSION_24
  sourceCompatibility = jv
  targetCompatibility = jv
  toolchain { languageVersion.set(JavaLanguageVersion.of(jv.ordinal + 1)) }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_24
    freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
  }
  jvmToolchain(24)
}

spotless {
  sql {
    lineEndings = LineEnding.UNIX
    target("**/**.sql")
    idea()
      .codeStyleSettingsPath(
        rootProject
          .layout
          .projectDirectory
          .file(".idea/codeStyles/Project.xml")
          .asFile
          .absolutePath
      )
  }
}

tasks {
  withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
  }
  withType<AbstractCopyTask> { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
  withType<JavaCompile> { options.compilerArgs.addAll(listOf("--enable-preview")) }
}
