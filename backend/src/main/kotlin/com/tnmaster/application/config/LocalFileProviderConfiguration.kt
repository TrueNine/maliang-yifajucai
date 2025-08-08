package com.tnmaster.application.config

import com.tnmaster.application.properties.FileProperties
import com.tnmaster.application.provider.SyncLocalFileProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FileProperties::class)
class LocalFileProviderConfiguration(private val fileProperties: FileProperties) {

  @Bean
  fun syncLocalFileProvider(): SyncLocalFileProvider {
    return SyncLocalFileProvider(fileProperties.logDir)
  }
}
