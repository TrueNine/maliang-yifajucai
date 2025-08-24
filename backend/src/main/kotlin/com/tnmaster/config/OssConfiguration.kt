package com.tnmaster.config

import io.github.truenine.composeserver.oss.IObjectStorageService
import io.github.truenine.composeserver.oss.minio.autoconfig.MinioAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(MinioAutoConfiguration::class)
class OssConfiguration {

  @Bean
  @ConditionalOnMissingBean
  fun objectStorageService(): IObjectStorageService {
    // 这里会由MinioAutoConfiguration自动创建
    // 如果MinioAutoConfiguration没有创建Bean，这里提供一个fallback
    throw IllegalStateException("ObjectStorageService should be created by MinioAutoConfiguration. Please check your OSS configuration.")
  }
}
