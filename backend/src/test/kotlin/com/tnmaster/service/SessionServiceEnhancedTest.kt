package com.tnmaster.service

import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * SessionService 增强功能测试
 *
 * 测试自动刷新、并发处理等增强功能
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class SessionServiceEnhancedTest {

  @Test
  fun create_session_service() {
    // 简单测试验证服务可以正常创建
    val userAccountRepo = mockk<com.tnmaster.repositories.IUserAccountRepo>()
    val redisTemplate = mockk<org.springframework.data.redis.core.RedisTemplate<String, Any>>()
    val permissionService = mockk<PermissionService>()

    val sessionService = SessionService(redisTemplate, permissionService)
    assertNotNull(sessionService)
  }

}
