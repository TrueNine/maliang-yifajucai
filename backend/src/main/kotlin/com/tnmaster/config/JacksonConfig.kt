package com.tnmaster.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.jackson.autoconfig.JacksonAutoConfiguration
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
  
  @Resource
  private lateinit var objectMapper: ObjectMapper
  
  @Resource(name = JacksonAutoConfiguration.NON_IGNORE_OBJECT_MAPPER_BEAN_NAME)
  private lateinit var nonJsonIgnoreObjectMapper: ObjectMapper
  
  @PostConstruct
  fun configureObjectMappers() {
    // 注册Kotlin模块以支持Kotlin data class和可空类型
    val kotlinModule = KotlinModule.Builder().build()

    // 确保两个 ObjectMapper 都支持 JSR310 模块和 Kotlin 模块
    objectMapper.registerModule(JavaTimeModule())
    objectMapper.registerModule(kotlinModule)

    nonJsonIgnoreObjectMapper.registerModule(JavaTimeModule())
    nonJsonIgnoreObjectMapper.registerModule(kotlinModule)

    // 添加调试日志
    println("Jackson配置完成 - 已注册Kotlin模块和JSR310模块")
    println("ObjectMapper模块数量: ${objectMapper.registeredModuleIds.size}")
    println("NonJsonIgnoreObjectMapper模块数量: ${nonJsonIgnoreObjectMapper.registeredModuleIds.size}")
  }
}
