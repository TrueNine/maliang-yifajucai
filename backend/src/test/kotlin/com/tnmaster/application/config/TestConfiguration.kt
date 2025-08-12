package com.tnmaster.application.config

import io.github.truenine.composeserver.oss.ObjectStorageService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@TestConfiguration
class TestWebMvcConfiguration {

  @Bean
  @Primary
  fun testWebMvcConfigurer(): WebMvcConfigurer {
    return object : WebMvcConfigurer {
      // 空的配置器，不添加任何拦截器
    }
  }
}

@TestConfiguration
class TestOssConfiguration {

  @Bean
  @Primary
  fun mockObjectStorageService(): ObjectStorageService {
    return mockk<ObjectStorageService>(relaxed = true)
  }
}
