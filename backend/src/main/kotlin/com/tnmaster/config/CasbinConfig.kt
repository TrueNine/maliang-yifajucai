package com.tnmaster.config

import com.tnmaster.security.CasbinDatabaseAdapter
import org.casbin.jcasbin.main.Enforcer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

/**
 * # Casbin 配置类
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Configuration
class CasbinConfig {

  @Bean
  fun enforcer(casbinDatabaseAdapter: CasbinDatabaseAdapter): Enforcer {
    val modelPath = ClassPathResource("casbin/model.conf").file.absolutePath
    val enforcer = Enforcer(modelPath, casbinDatabaseAdapter)

    // 启用自动保存策略
    enforcer.enableAutoSave(true)

    // 启用日志
    enforcer.enableLog(true)

    return enforcer
  }
}
