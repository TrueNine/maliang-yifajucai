package com.tnmaster.application.service

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil
import cn.dev33.satoken.servlet.util.SaTokenContextJakartaServletUtil
import cn.dev33.satoken.stp.StpUtil
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.service.SaTokenService
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
class SaTokenServiceTest : IDatabasePostgresqlContainer, ICacheRedisContainer {

  @Resource
  lateinit var saTokenService: SaTokenService

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @Test
  @RDBRollback
  fun `toDisabled creates correct disable wrapper info`() {
    // Given: future datetime for ban expiry
    val banExpiry = datetime.now().plusHours(2)
    
    // When: create disable info
    val disableInfo = saTokenService.toDisabled(
      disabledToDatetime = banExpiry,
      disabled = true,
      level = 2
    )
    
    // Then: wrapper should reflect ban state
    assertTrue(disableInfo.isDisable)
    assertTrue(disableInfo.disableTime > 0) // should have positive seconds
    assertEquals(2, disableInfo.disableLevel)
  }

  @Test
  @RDBRollback
  fun `toDisabled with null datetime creates non-disabled state`() {
    // When: create disable info with null datetime
    val disableInfo = saTokenService.toDisabled(
      disabledToDatetime = null,
      disabled = false,
      level = 0
    )
    
    // Then: should not be disabled
    assertFalse(disableInfo.isDisable)
    assertEquals(0, disableInfo.disableTime)
    assertEquals(0, disableInfo.disableLevel)
  }

  @Test
  @RDBRollback
  fun `session login and logout operations work correctly`() {
    SaTokenContextMockUtil.setMockContext {
      val req = MockHttpServletRequest()
      val resp = MockHttpServletResponse()
      SaTokenContextJakartaServletUtil.setContext(req, resp)
      
      val testAccount = "test_user_123"
      
      // When: login
      saTokenService.setCurrentSessionToLogin(testAccount)
      
      // Then: should be logged in
      assertTrue(StpUtil.isLogin())
      assertEquals(testAccount, StpUtil.getLoginId().toString())
      
      // When: logout
      saTokenService.setCurrentSessionToLogout()
      
      // Then: should be logged out
      assertFalse(StpUtil.isLogin())
    }
  }
}

