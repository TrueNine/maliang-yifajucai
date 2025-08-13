package com.tnmaster.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
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
    // Configure both ObjectMappers with enhanced Kotlin and polymorphic type support
    configureObjectMapper(objectMapper)
    configureObjectMapper(nonJsonIgnoreObjectMapper)

    // 添加调试日志
    println("Enhanced Jackson配置完成 - 已注册Kotlin模块、JSR310模块和多态类型支持")
    println("ObjectMapper模块数量: ${objectMapper.registeredModuleIds.size}")
    println("NonJsonIgnoreObjectMapper模块数量: ${nonJsonIgnoreObjectMapper.registeredModuleIds.size}")
  }

  private fun configureObjectMapper(mapper: ObjectMapper) {
    // Enhanced Kotlin module configuration for better data class support
    val kotlinModule = KotlinModule.Builder()
      .withReflectionCacheSize(512)
      .configure(KotlinFeature.NullToEmptyCollection, false)
      .configure(KotlinFeature.NullToEmptyMap, false)
      .configure(KotlinFeature.NullIsSameAsDefault, false)
      .configure(KotlinFeature.SingletonSupport, false)
      .configure(KotlinFeature.StrictNullChecks, false)
      .build()

    // Register modules for comprehensive type support
    mapper.registerModule(kotlinModule)
    mapper.registerModule(JavaTimeModule())

    // Create custom module for datetime serialization
    val datetimeModule = SimpleModule("DatetimeModule")
    datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
    datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
    mapper.registerModule(datetimeModule)

    // Configure deserialization features for robust error handling
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
    mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)

    // Configure serialization features for consistent output
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)

    // Configure polymorphic type handling with @class information
    // This is crucial for Redis serialization with complex object hierarchies
    // Use NON_FINAL for interfaces and abstract classes, explicit annotations for concrete classes
    mapper.activateDefaultTyping(
      LaissezFaireSubTypeValidator.instance,
      ObjectMapper.DefaultTyping.NON_FINAL,
      JsonTypeInfo.As.PROPERTY
    )
  }
}
