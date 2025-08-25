package com.tnmaster.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.protocol.ProtocolVersion
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfiguration {

  @Bean
  fun lettuceClientConfigurationBuilderCustomizer(): LettuceClientConfigurationBuilderCustomizer {
    return LettuceClientConfigurationBuilderCustomizer { builder ->
      builder.clientOptions(
        ClientOptions.builder()
          .autoReconnect(true)
          .protocolVersion(ProtocolVersion.RESP2)
          .pingBeforeActivateConnection(false)
          .build()
      )
    }
  }
}