package com.tnmaster.service

import com.tnmaster.repositories.IUserAccountRepo
import io.github.truenine.composeserver.RefId
import com.tnmaster.security.SessionData
import com.tnmaster.security.UserPrincipal
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.servlet.deviceId
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.AfterEach
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import io.mockk.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

/**
 * SessionService 单元测试
 *
 * @author TrueNine
 * @since 2025-01-10
 */
class SessionServiceTest {

  private lateinit var userAccountRepo: IUserAccountRepo
  private lateinit var redisTemplate: RedisTemplate<String, Any>
  private lateinit var permissionService: PermissionService
  private lateinit var sessionService: SessionService
  private lateinit var request: HttpServletRequest
  private lateinit var valueOps: ValueOperations<String, Any>

  @BeforeEach
  fun setUp() {
    userAccountRepo = mockk()
    redisTemplate = mockk()
    permissionService = mockk()
    request = mockk(relaxed = true)
    valueOps = mockk()

    every { redisTemplate.opsForValue() } returns valueOps
    every { request.getHeader("User-Agent") } returns "Test Browser"
    every { request.getHeader("X-Client-Version") } returns "1.0.0"
    every { request.getHeader("X-Forwarded-For") } returns "127.0.0.1"
    every { request.getHeader("X-Real-IP") } returns "127.0.0.1"
    every { request.getHeader("Proxy-Client-IP") } returns null
    every { request.getHeader("WL-Proxy-Client-IP") } returns null
    every { request.getHeader("HTTP_CLIENT_IP") } returns null
    every { request.getHeader("HTTP_X_FORWARDED_FOR") } returns null
    every { request.remoteAddr } returns "127.0.0.1"

    // Mock static extension functions
    mockkStatic("io.github.truenine.composeserver.depend.servlet.HttpServletRequestFnsKt")

    sessionService = SessionService(redisTemplate, permissionService)
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("io.github.truenine.composeserver.depend.servlet.HttpServletRequestFnsKt")
  }

  @Test
  fun generate_session_id() {
    // When
    val sessionId1 = sessionService.generateSessionId()
    val sessionId2 = sessionService.generateSessionId()

    // Then
    assertAll(
      { assertNotNull(sessionId1) },
      { assertNotNull(sessionId2) },
      { assertTrue(sessionId1.length >= 32) },
      { assertTrue(sessionId2.length >= 32) },
      { assertFalse(sessionId1 == sessionId2) }
    )
  }

  @Test
  fun create_user_session() {
    // Given
    val account = "testuser"
    val userId = 1L
    val roles = setOf("user", "editor")
    val permissions = setOf("read", "write")

    // Mock extension properties for this test
    every { request.deviceId } returns "test-device-123"
    every { request.remoteRequestIp } returns "127.0.0.1"

    // Mock valueOps.set method
    every { valueOps.set(any<String>(), any<SessionData>(), any<Long>(), any<TimeUnit>()) } returns Unit

    // Mock valueOps.get method for verification - 返回模拟的SessionData对象
    every { valueOps.get(any<String>()) } returns createTestSessionData("test-session", account, userId, roles, permissions)

    // When
    val sessionId = sessionService.createUserSession(account, userId, request, roles, permissions)

    // Then
    assertNotNull(sessionId)
    verify { valueOps.set(eq("session:$sessionId"), any<SessionData>(), eq(86400L), eq(TimeUnit.SECONDS)) }
    verify { valueOps.set(eq("user:session:$account"), any<SessionData>(), eq(86400L), eq(TimeUnit.SECONDS)) }
    // 验证存储验证调用
    verify { valueOps.get(eq("session:$sessionId")) }
  }

  @Test
  fun validate_valid_session() {
    // Given
    val sessionId = "valid-session-123"
    val account = "testuser"
    val userId = 1L
    val roles = setOf("user")
    val permissions = setOf("read")

    val sessionData = createTestSessionData(sessionId, account, userId, roles, permissions)
    every { valueOps.get("session:$sessionId") } returns sessionData
    every { redisTemplate.hasKey("user:disabled:$account") } returns false
    every { permissionService.getUserRoles(account) } returns listOf("user")
    every { permissionService.getRolePermissions("user") } returns listOf(listOf("user", "read", "allow"))

    // When
    val user = sessionService.validateSessionAndGetUser(sessionId)

    // Then
    assertNotNull(user)
    assertEquals(account, user.account)
    assertEquals(userId, user.userId)
    assertTrue(user.roles.contains("user"))
    assertTrue(user.permissions.contains("read"))
  }

  @Test
  fun validate_invalid_session() {
    // Given
    val sessionId = "invalid-session-123"
    every { valueOps.get("session:$sessionId") } returns null

    // When
    val user = sessionService.validateSessionAndGetUser(sessionId)

    // Then
    assertNull(user)
  }

  @Test
  fun validate_expired_session() {
    // Given
    val sessionId = "expired-session-123"
    val account = "testuser"
    val userId = 1L
    val expiredSessionData = createTestSessionData(
      sessionId,
      account,
      userId,
      setOf("user"),
      setOf("read"),
      datetime.now().minusSeconds(1)
    )

    every { valueOps.get("session:$sessionId") } returns expiredSessionData
    every { redisTemplate.delete("session:$sessionId") } returns true

    // When
    val user = sessionService.validateSessionAndGetUser(sessionId)

    // Then
    assertNull(user)
    verify { redisTemplate.delete("session:$sessionId") }
  }

  @Test
  fun refresh_session() {
    // Given
    val sessionId = "valid-session-123"
    val account = "testuser"
    val userId = 1L
    val sessionData = createTestSessionData(sessionId, account, userId, setOf("user"), setOf("read"))

    every { valueOps.get("session:$sessionId") } returns sessionData
    every { valueOps.set(any<String>(), any<SessionData>(), any<Long>(), any<TimeUnit>()) } returns Unit

    // When
    val result = sessionService.refreshSession(sessionId)

    // Then
    assertTrue(result)
    verify { valueOps.set(eq("session:$sessionId"), any<SessionData>(), eq(86400L), eq(TimeUnit.SECONDS)) }
    verify { valueOps.set(eq("user:session:$account"), any<SessionData>(), eq(86400L), eq(TimeUnit.SECONDS)) }
  }

  @Test
  fun refresh_nonexistent_session() {
    // Given
    val sessionId = "non-existent-session"
    every { valueOps.get("session:$sessionId") } returns null

    // When
    val result = sessionService.refreshSession(sessionId)

    // Then
    assertFalse(result)
    verify(exactly = 0) { valueOps.set(any(), any(), any(), any()) }
  }

  @Test
  fun user_logout() {
    // Given
    val sessionId = "valid-session-123"
    val account = "testuser"
    val sessionData = createTestSessionData(sessionId, account, 1L, setOf("user"), setOf("read"))

    every { valueOps.get("session:$sessionId") } returns sessionData
    every { redisTemplate.delete(any<String>()) } returns true

    // When
    sessionService.logout(sessionId)

    // Then
    verify { redisTemplate.delete("session:$sessionId") }
    verify { redisTemplate.delete("user:session:$account") }
  }

  @Test
  fun check_session_exists() {
    // Given
    val sessionId = "test-session-123"
    every { redisTemplate.hasKey("session:$sessionId") } returns true

    // When
    val exists = sessionService.sessionExists(sessionId)

    // Then
    assertTrue(exists)
    verify { redisTemplate.hasKey("session:$sessionId") }
  }

  @Test
  fun get_session_data() {
    // Given
    val sessionId = "test-session-123"
    val sessionData = createTestSessionData(sessionId, "testuser", 1L, setOf("user"), setOf("read"))
    every { valueOps.get("session:$sessionId") } returns sessionData

    // When
    val result = sessionService.getSessionData(sessionId)

    // Then
    assertNotNull(result)
    assertEquals(sessionId, result.sessionId)
  }

  @Test
  fun get_user_session() {
    // Given
    val account = "testuser"
    val sessionData = createTestSessionData("session-123", account, 1L, setOf("user"), setOf("read"))
    every { valueOps.get("user:session:$account") } returns sessionData

    // When
    val result = sessionService.getUserSession(account)

    // Then
    assertNotNull(result)
    assertEquals(account, result.account)
  }

  private fun createTestSessionData(
    sessionId: String,
    account: String,
    userId: Long,
    roles: Set<String>,
    permissions: Set<String>,
    expireTime: datetime = datetime.now().plusSeconds(3600),
  ): SessionData {
    return SessionData(
      sessionId = sessionId,
      account = account,
      userId = userId,
      deviceId = "test-device",
      loginIpAddr = "127.0.0.1",
      loginTime = datetime.now(),
      roles = roles,
      permissions = permissions,
      expireTime = expireTime
    )
  }
}
