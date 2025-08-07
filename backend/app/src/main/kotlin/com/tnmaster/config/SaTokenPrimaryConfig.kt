package com.tnmaster.config

import cn.dev33.satoken.config.SaTokenConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class SaTokenPrimaryConfig {

  @Bean
  @Primary
  fun saTokenConfigPrimary(): SaTokenConfig {
    val cfg = SaTokenConfig()

    cfg.tokenName = "Authorization"
    cfg.timeout = 7 * 24 * 60 * 60
    cfg.activeTimeout = 1 * 24 * 60 * 60

    cfg.isConcurrent = true
    cfg.isShare = false
    cfg.maxLoginCount = 1

    cfg.isReadBody = false
    cfg.isReadCookie = false
    cfg.isReadHeader = true
    cfg.isWriteHeader = true

    cfg.dataRefreshPeriod = 3600
    cfg.tokenSessionCheckLogin = true
    cfg.autoRenew = true

    cfg.isPrint = false

    cfg.tokenStyle = "simple-uuid"
    return cfg
  }
}
