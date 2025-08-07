package com.tnmaster.config

import com.tnmaster.properties.FileProperties
import com.tnmaster.provider.SyncLocalFileProvider
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
