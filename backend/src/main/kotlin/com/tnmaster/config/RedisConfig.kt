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
   * 在测试环境下，该Bean的优先级会被TestRedisConfiguration覆盖
   */
  @Bean("redisTemplate")
  @Primary
  fun redisTemplate(
    connectionFactory: RedisConnectionFactory,
    redisSerializationErrorHandler: RedisSerializationErrorHandler,
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
    redisSerializationErrorHandler: RedisSerializationErrorHandler,
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

      // 创建自定义验证器以排除datetime类型的多态处理
      val customValidator = object : com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator() {
        override fun validateBaseType(
          config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?,
          baseType: com.fasterxml.jackson.databind.JavaType?,
        ): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
          if (baseType?.rawClass == datetime::class.java) {
            return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
          }
          return LaissezFaireSubTypeValidator.instance.validateBaseType(config, baseType)
        }

        override fun validateSubClassName(
          config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?,
          baseType: com.fasterxml.jackson.databind.JavaType?,
          subClassName: String?,
        ): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
          if (subClassName?.contains("datetime") == true) {
            return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
          }
          return LaissezFaireSubTypeValidator.instance.validateSubClassName(config, baseType, subClassName)
        }

        override fun validateSubType(
          config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?,
          baseType: com.fasterxml.jackson.databind.JavaType?,
          subType: com.fasterxml.jackson.databind.JavaType?,
        ): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
          if (subType?.rawClass == datetime::class.java) {
            return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
          }
          return LaissezFaireSubTypeValidator.instance.validateSubType(config, baseType, subType)
        }
      }

      // 启用多态类型处理以支持@JsonTypeInfo注解
      activateDefaultTyping(
        customValidator,
        ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
        JsonTypeInfo.As.PROPERTY
      )

      // 配置反序列化特性以提高兼容性
      configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
      configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
      configure(com.fasterxml.jackson.databind.DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, false)
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
