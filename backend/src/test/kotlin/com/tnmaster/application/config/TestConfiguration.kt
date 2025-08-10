package com.tnmaster.application.config

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
