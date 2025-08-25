package com.tnmaster.interceptors

import com.tnmaster.service.SessionService
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * SessionAuthenticationInterceptor 单元测试
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class SessionAuthenticationInterceptorTest {

  @Test
  fun `拦截器能正常创建`() {
    // 简单测试验证拦截器可以正常创建
    val sessionService = mockk<SessionService>()
    val interceptor = SessionAuthenticationInterceptor(sessionService)
    assertNotNull(interceptor)
  }
}
