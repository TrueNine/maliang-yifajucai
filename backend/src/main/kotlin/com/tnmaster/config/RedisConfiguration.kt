package com.tnmaster.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.protocol.ProtocolVersion
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfiguration {

  @Bean
  fun lettuceClientConfigurationBuilderCustomizer(): LettuceClientConfigurationBuilderCustomizer {
    return LettuceClientConfigurationBuilderCustomizer { builder ->
      builder
        .clientOptions(
          ClientOptions.builder()
            .autoReconnect(true)
            .protocolVersion(ProtocolVersion.RESP2)
            .pingBeforeActivateConnection(true)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))
            .build()
        )
        .commandTimeout(Duration.ofSeconds(5))
    }
  }

  @Bean
  @Primary
  fun redisConnectionFactory(): RedisConnectionFactory {
    val config = RedisStandaloneConfiguration().apply {
      hostName = "127.0.0.1"
      port = 6379
      password = RedisPassword.of("redis123")
      database = 0
    }

    val clientConfig = ClientOptions.builder()
      .autoReconnect(true)
      .protocolVersion(ProtocolVersion.RESP2)
      .pingBeforeActivateConnection(true)
      .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
      .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(5)))
      .build()

    val lettuceConfig = org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.builder()
      .clientOptions(clientConfig)
      .commandTimeout(Duration.ofSeconds(5))
      .build()

    return LettuceConnectionFactory(config, lettuceConfig)
  }

  @Bean(name = ["yfjc_redisTemplate"])
  @Primary
  fun redisTemplate(
    redisConnectionFactory: RedisConnectionFactory,
  ): RedisTemplate<String, *> {
    val template = RedisTemplate<String, Any>()
    template.connectionFactory = redisConnectionFactory

    val serializer = JdkSerializationRedisSerializer()

    // 配置序列化器
    template.keySerializer = StringRedisSerializer()
    template.hashKeySerializer = StringRedisSerializer()
    template.valueSerializer = serializer
    template.hashValueSerializer = serializer

    template.afterPropertiesSet()
    return template
  }
}
