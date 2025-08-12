package com.tnmaster.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.tnmaster.entities.ApiCallRecord
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



  @Bean("apiCallRecordRedisTemplate")
  fun apiCallRecordRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, ApiCallRecord?> {
    val template = RedisTemplate<String, ApiCallRecord?>()
    template.connectionFactory = connectionFactory

    // 创建一个专门用于ApiCallRecord的ObjectMapper
    val objectMapper = ObjectMapper()

    // 注册Kotlin模块以支持Kotlin data class和可空类型
    val kotlinModule = KotlinModule.Builder().build()
    objectMapper.registerModule(kotlinModule)
    objectMapper.registerModule(JavaTimeModule())

    // 注册Jimmer模块以支持Jimmer实体序列化
    objectMapper.registerModule(ImmutableModule())

    // 创建自定义模块来处理datetime类型
    val datetimeModule = SimpleModule()
    datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
    datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
    objectMapper.registerModule(datetimeModule)

    // 使用Jackson2JsonRedisSerializer避免类型信息问题
    val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, ApiCallRecord::class.java)

    // 设置序列化器
    template.keySerializer = StringRedisSerializer()
    template.valueSerializer = jsonSerializer
    template.hashKeySerializer = StringRedisSerializer()
    template.hashValueSerializer = jsonSerializer

    template.afterPropertiesSet()

    return template
  }
}
