package com.tnmaster.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
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

  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
    val template = RedisTemplate<String, Any>()
    template.connectionFactory = connectionFactory

    // 设置序列化器
    template.keySerializer = StringRedisSerializer()
    template.valueSerializer = GenericJackson2JsonRedisSerializer()
    template.hashKeySerializer = StringRedisSerializer()
    template.hashValueSerializer = GenericJackson2JsonRedisSerializer()

    template.afterPropertiesSet()
    return template
  }
}
