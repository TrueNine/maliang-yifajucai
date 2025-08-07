package com.tnmaster.config

import com.yomahub.liteflow.core.FlowExecutor
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy

@AutoConfiguration
class LiteFlowConfig {
  @Bean
  @Lazy
  @ConditionalOnMissingBean
  fun ideSupportFlowExecutor(): FlowExecutor {
    return FlowExecutor()
  }
}
