import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  java
  idea
  alias(cs.plugins.io.github.truenine.composeserver.gradle.plugin)
  alias(cs.plugins.org.springframework.boot)
  alias(cs.plugins.org.jetbrains.kotlin.jvm)
  alias(cs.plugins.org.jetbrains.kotlin.plugin.spring)
  alias(cs.plugins.com.diffplug.spotless)
  alias(cs.plugins.com.google.devtools.ksp)
  alias(cs.plugins.io.spring.dependency.management)
}

group = "com.tnmaster"
version = "1.0"

idea {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

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
  implementation(cs.org.springframework.boot.spring.boot.docker.compose)

  implementation(platform(cs.org.springframework.boot.spring.boot.dependencies))
  implementation(platform(cs.org.springframework.boot.spring.boot.dependencies))
  implementation(platform(cs.org.springframework.cloud.spring.cloud.dependencies))
  implementation(platform(cs.org.springframework.modulith.spring.modulith.bom))

  // Casbin 权限控制
  implementation(cs.org.casbin.jcasbin)
  implementation(cs.org.casbin.casbin.spring.boot.starter)

  // AOP 支持
  implementation("org.springframework.boot:spring-boot-starter-aop")


  implementation(cs.org.springframework.boot.spring.boot.starter.data.redis)
  implementation(cs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
  implementation(cs.org.babyfish.jimmer.jimmer.spring.boot.starter)

  implementation(cs.com.yomahub.liteflow.spring.boot.starter) { exclude(group = "cn.hutool", module = "hutool-core") }

  implementation(cs.io.github.truenine.composeserver.cacheable)
  implementation(cs.io.github.truenine.composeserver.psdk.wxpa)

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
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

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

tasks {
  withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
  }
  withType<AbstractCopyTask> { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
  withType<JavaCompile> { options.compilerArgs.addAll(listOf("--enable-preview")) }
}


// 从根目录 .env 加载并注入到常见运行类任务（Test/JavaExec/BootRun/run 等）
fun parseDotenv(file: File): Map<String, String> {
  if (!file.exists() || !file.canRead()) return emptyMap()
  val result = mutableMapOf<String, String>()
  file.readLines().forEach { raw ->
    val line = raw.trim()
    if (line.isEmpty() || line.startsWith("#")) return@forEach
    val i = line.indexOf('=')
    if (i <= 0) return@forEach
    val key = line.take(i).trim()
    var value = line.substring(i + 1).trim()
    if (value.length >= 2 && value.startsWith('"') && value.endsWith('"')) {
      value = value.substring(1, value.length - 1)
        .replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\\", "\\")
    } else if (value.length >= 2 && value.startsWith('\'') && value.endsWith('\'')) {
      value = value.substring(1, value.length - 1)
    }
    if (key.isNotBlank()) result[key] = value
  }
  return result
}

val rootDotenv = rootProject.layout.projectDirectory.file("../.env").asFile.takeIf { it.exists() }?.let {
  parseDotenv(it)
}

if (rootDotenv?.isNotEmpty() == true) {
  tasks.withType(org.springframework.boot.gradle.tasks.run.BootRun::class.java).configureEach {
    rootDotenv.forEach { (k, v) -> environment(k, v) }
  }
  tasks.withType(Test::class.java).configureEach {
    rootDotenv.forEach { (k, v) -> environment(k, v) }
  }
  tasks.withType(JavaExec::class.java).configureEach {
    rootDotenv.forEach { (k, v) -> environment(k, v) }
  }
} else {
  logger.error("[dotenv] 根目录 ../.env 未找到或为空，跳过注入")
}
