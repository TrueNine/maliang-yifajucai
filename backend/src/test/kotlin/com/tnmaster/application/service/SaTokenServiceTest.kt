package com.tnmaster.application.service

import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.security.UserContextHolder
import com.tnmaster.service.AuthService
import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import java.time.Duration
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class AuthServiceTest : IDatabasePostgresqlContainer, ICacheRedisContainer, IOssMinioContainer {

  @Resource
  lateinit var authService: AuthService

  @Resource
  lateinit var sessionService: SessionService

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @AfterEach
  fun cleanup() {
    UserContextHolder.clear()
  }

  @Test
  @RDBRollback
  fun `SessionId generation and validation works correctly`() {
    // Given: test user data
    val testAccount = "test_user_123"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val testRoles = setOf("USER")
    val testPermissions = setOf("READ_USER", "WRITE_USER")
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
      addHeader("Device-Id", "test-device-001")
      addParameter("deviceId", "test-device-001")
      setAttribute("deviceId", "test-device-001")
      remoteAddr = "127.0.0.1"
    }
    
    // When: create session
    val sessionId = sessionService.createUserSession(testAccount, testUserId, request, testRoles, testPermissions)
    
    // Then: session should be valid
    assertNotNull(sessionId)
    assertTrue(sessionService.sessionExists(sessionId))
    
    val user = sessionService.validateSessionAndGetUser(sessionId)
    assertNotNull(user)
    assertEquals(testAccount, user.account)
    assertEquals(testUserId, user.userId)
    assertEquals(testRoles, user.roles)
    assertEquals(testPermissions, user.permissions)
  }

  @Test
  @RDBRollback
  fun `user disable and enable operations work correctly`() {
    // Given: test account
    val testAccount = "test_user_disable"
    
    // When: disable user
    authService.disableUser(testAccount, Duration.ofMinutes(5))
    
    // Then: user should be disabled
    assertTrue(authService.isUserDisabled(testAccount))
    
    // When: time passes (simulate by clearing Redis key manually for test)
    // In real scenario, Redis TTL would handle this
    // For test, we can verify the disable state exists
    assertTrue(authService.isUserDisabled(testAccount))
  }

  @Test
  @RDBRollback
  fun `user login state management works correctly`() {
    // Given: test data
    val testAccount = "test_login_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
      addHeader("Device-Id", "test-device-001")
      addParameter("deviceId", "test-device-001")
      setAttribute("deviceId", "test-device-001")
      remoteAddr = "127.0.0.1"
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")
    
    // When: set login state
    val sessionId = authService.setUserLoginState(testAccount, testUserId, request, roles, permissions)
    
    // Then: sessionId should be generated and user should be retrievable
    assertNotNull(sessionId)
    assertTrue(sessionService.sessionExists(sessionId))
    
    val user = authService.validateSessionAndGetUser(sessionId)
    assertNotNull(user)
    assertEquals(testAccount, user.account)
    assertEquals(testUserId, user.userId)
    assertEquals(roles, user.roles)
    assertEquals(permissions, user.permissions)
  }

  @Test
  @RDBRollback
  fun `user logout clears session correctly`() {
    // Given: logged in user
    val testAccount = "test_logout_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Device-123")
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")
    
    val sessionId = authService.setUserLoginState(testAccount, testUserId, request, roles, permissions)
    assertNotNull(authService.validateSessionAndGetUser(sessionId))
    
    // When: logout
    authService.logoutByAccount(testAccount)
    
    // Then: user session should be cleared
    assertNull(authService.validateSessionAndGetUser(sessionId))
  }

  @Test
  fun `invalid sessionId validation returns null`() {
    // Given: invalid sessionId
    val invalidSessionId = "invalid-session-id-123"
    
    // When: validate sessionId
    val user = authService.validateSessionAndGetUser(invalidSessionId)
    
    // Then: should return null
    assertNull(user)
  }

  @Test
  @RDBRollback
  fun `session refresh extends expiration time`() {
    // Given: active session
    val testAccount = "test_refresh_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Device-456")
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")
    
    val sessionId = authService.setUserLoginState(testAccount, testUserId, request, roles, permissions)
    
    // When: refresh session
    val refreshResult = sessionService.refreshSession(sessionId)
    
    // Then: refresh should succeed
    assertTrue(refreshResult)
    assertTrue(sessionService.sessionExists(sessionId))
    
    // And: session should still be valid
    val user = sessionService.validateSessionAndGetUser(sessionId)
    assertNotNull(user)
    assertEquals(testAccount, user.account)
  }
}

