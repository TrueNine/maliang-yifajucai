package com.tnmaster.application.service

import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@Testcontainers
class SessionServiceTest : IDatabasePostgresqlContainer, ICacheRedisContainer, IOssMinioContainer {

  @Resource
  lateinit var sessionService: SessionService

  @Test
  fun `sessionId generation and validation works correctly`() {
    // Given: test user data
    val testAccount = "test_user_123"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val testRoles = setOf("USER")
    val testPermissions = setOf("READ_USER", "WRITE_USER")
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
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
  fun `invalid sessionId validation returns null`() {
    // Given: invalid sessionId
    val invalidSessionId = "invalid-session-id-123"

    // When: validate sessionId
    val user = sessionService.validateSessionAndGetUser(invalidSessionId)

    // Then: should return null
    assertNull(user)
  }

  @Test
  fun `session refresh extends expiration time`() {
    // Given: active session
    val testAccount = "test_refresh_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")

    val sessionId = sessionService.createUserSession(testAccount, testUserId, request, roles, permissions)

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

  @Test
  fun `user logout clears session correctly`() {
    // Given: logged in user
    val testAccount = "test_logout_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")

    val sessionId = sessionService.createUserSession(testAccount, testUserId, request, roles, permissions)
    assertNotNull(sessionService.validateSessionAndGetUser(sessionId))

    // When: logout
    sessionService.logoutByAccount(testAccount)

    // Then: user session should be cleared
    assertNull(sessionService.validateSessionAndGetUser(sessionId))
  }
}

