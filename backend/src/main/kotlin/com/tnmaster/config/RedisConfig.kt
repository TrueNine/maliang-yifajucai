package com.tnmaster.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.tnmaster.entities.ApiCallRecord
import com.tnmaster.config.redis.ErrorHandlingRedisSerializer
import com.tnmaster.config.redis.RedisSerializationErrorHandler
import io.github.truenine.composeserver.datetime
import org.babyfish.jimmer.jackson.ImmutableModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

  @Value($$"${spring.data.redis.host:127.0.0.1}")
  private lateinit var host: String

  @Value($$"${spring.data.redis.port:6379}")
  private var port: Int = 6379

  @Value($$"${spring.data.redis.password:}")
  private var password: String = ""

  @Value("\${spring.data.redis.database:0}")
  private var database: Int = 0

  @Bean
  fun redisConnectionFactory(): RedisConnectionFactory {
    val config = RedisStandaloneConfiguration()
    config.hostName = host
    config.port = port
    config.database = database

    if (password.isNotEmpty()) {
      config.password = RedisPassword.of(password)
    }

    return LettuceConnectionFactory(config)
  }

  /**
   * 主要的RedisTemplate配置，用于通用对象序列化
   * 这个配置将确保与测试环境的一致性，正确处理@class类型信息
   */
  @Bean("redisTemplate")
  @Primary
  fun redisTemplate(
    connectionFactory: RedisConnectionFactory,
    redisSerializationErrorHandler: RedisSerializationErrorHandler
  ): RedisTemplate<String, Any> {
    val template = RedisTemplate<String, Any>()
    template.connectionFactory = connectionFactory

    // 创建统一的ObjectMapper用于Redis序列化
    val objectMapper = createRedisObjectMapper()

    // 使用带错误处理的Redis序列化器
    val jsonSerializer = ErrorHandlingRedisSerializer(objectMapper, redisSerializationErrorHandler)

    // 设置序列化器
    template.keySerializer = StringRedisSerializer()
    template.valueSerializer = jsonSerializer
    template.hashKeySerializer = StringRedisSerializer()
    template.hashValueSerializer = jsonSerializer

    template.afterPropertiesSet()

    return template
  }

  @Bean("apiCallRecordRedisTemplate")
  fun apiCallRecordRedisTemplate(
    connectionFactory: RedisConnectionFactory,
    redisSerializationErrorHandler: RedisSerializationErrorHandler
  ): RedisTemplate<String, ApiCallRecord?> {
    val template = RedisTemplate<String, ApiCallRecord?>()
    template.connectionFactory = connectionFactory

    // 使用相同的ObjectMapper确保序列化一致性
    val objectMapper = createRedisObjectMapper()

    // 使用共享的错误处理器实例
    val jsonSerializer = ErrorHandlingRedisSerializer(objectMapper, redisSerializationErrorHandler)

    // 设置序列化器
    template.keySerializer = StringRedisSerializer()
    template.valueSerializer = jsonSerializer
    template.hashKeySerializer = StringRedisSerializer()
    template.hashValueSerializer = jsonSerializer

    template.afterPropertiesSet()

    return template
  }

  /**
   * 创建用于Redis序列化的统一ObjectMapper
   * 确保与测试环境配置一致，正确处理@class类型信息
   */
  private fun createRedisObjectMapper(): ObjectMapper {
    return ObjectMapper().apply {
      // 注册Kotlin模块以支持Kotlin data class和可空类型
      val kotlinModule = KotlinModule.Builder().build()
      registerModule(kotlinModule)
      registerModule(JavaTimeModule())

      // 注册Jimmer模块以支持Jimmer实体序列化
      registerModule(ImmutableModule())

      // 创建自定义模块来处理datetime类型
      val datetimeModule = SimpleModule()
      datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
      datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
      registerModule(datetimeModule)

      // 配置多态类型处理，确保@class属性正确包含
      // 这是解决"missing type id property '@class'"问题的关键配置
      activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY
      )
    }
  }

  /**
   * Redis序列化错误处理器
   */
  @Bean
  fun redisSerializationErrorHandler(): RedisSerializationErrorHandler {
    return RedisSerializationErrorHandler()
  }
}
