package com.tnmaster.application.application

import org.babyfish.jimmer.spring.repository.EnableJimmerRepositories
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableJimmerRepositories(basePackages = ["com.tnmaster.application.repositories"])
internal class TnMasterRunner

/**
 * The main entry point of the Spring Boot application.
 *
 * @param args command-line arguments passed to the application
 */
fun main(args: Array<String>) {
  runApplication<TnMasterRunner>(*args)
}
