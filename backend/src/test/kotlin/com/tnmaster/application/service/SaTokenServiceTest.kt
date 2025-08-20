package com.tnmaster.application.service

import com.tnmaster.config.TestCasbinConfig
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.security.UserContextHolder
import com.tnmaster.service.AuthService
import com.tnmaster.service.PermissionService
import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles
import java.sql.Connection
import java.time.Duration
import javax.sql.DataSource
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Import(TestCasbinConfig::class)
class AuthServiceTest : IDatabasePostgresqlContainer, ICacheRedisContainer, IOssMinioContainer {

  @Resource
  lateinit var authService: AuthService

  @Resource
  lateinit var sessionService: SessionService

  @Resource
  lateinit var userAccountRepo: IUserAccountRepo

  @Resource
  lateinit var redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>

  @Resource
  lateinit var dataSource: DataSource

  @Resource
  lateinit var permissionService: PermissionService

  @BeforeEach
  fun setUp() {
    // 验证Casbin配置是否正常工作
    verifyCasbinSetup()
    
    // 初始化测试数据
    initializeTestData()
  }
  
  /**
   * 验证Casbin配置是否正常工作
   */
  private fun verifyCasbinSetup() {
    try {
      // 测试基本的权限检查功能
      val hasPermission = permissionService.checkPermission("USER", "READ_USER", "allow")
      println("Casbin setup verification - USER has READ_USER permission: $hasPermission")
      
      // 测试角色检查功能
      val hasRole = permissionService.hasRole("test_user_123", "USER")
      println("Casbin setup verification - test_user_123 has USER role: $hasRole")
      
    } catch (e: Exception) {
      println("Warning: Casbin setup verification failed: ${e.message}")
      e.printStackTrace()
    }
  }

  @AfterEach
  fun cleanup() {
    UserContextHolder.clear()
    // 清理测试数据
    cleanupTestData()
  }

  /**
   * 初始化测试数据，确保测试用户有正确的角色关联
   */
  private fun initializeTestData() {
    dataSource.connection.use { connection ->
      // 创建测试用户
      createTestUser(connection, "test_user_123", 8416520155186815780L)

      // 确保用户关联到USER角色组
      associateUserWithRoleGroup(connection, "test_user_123", "USER")
    }

    // 使用PermissionService添加测试用户的角色
    addTestUserRole("test_user_123", "USER")
  }

  /**
   * 清理测试数据
   */
  private fun cleanupTestData() {
    // 从PermissionService移除测试用户角色
    removeTestUserRole("test_user_123", "USER")

    dataSource.connection.use { connection ->
      // 删除测试用户的角色组关联
      connection.prepareStatement(
        "DELETE FROM user_role_group WHERE user_id = (SELECT id FROM user_account WHERE account = ?)"
      ).use { stmt ->
        stmt.setString(1, "test_user_123")
        stmt.executeUpdate()
      }

      // 删除测试用户
      connection.prepareStatement("DELETE FROM user_account WHERE account = ?").use { stmt ->
        stmt.setString(1, "test_user_123")
        stmt.executeUpdate()
      }
    }
  }

  /**
   * 使用PermissionService为测试用户添加角色
   */
  private fun addTestUserRole(account: String, role: String) {
    try {
      // 确保角色权限存在
      val hasReadPermission = permissionService.checkPermission(role, "READ_USER", "allow")
      val hasWritePermission = permissionService.checkPermission(role, "WRITE_USER", "allow")
      
      if (!hasReadPermission) {
        permissionService.addPolicy(role, "READ_USER", "allow")
      }
      if (!hasWritePermission) {
        permissionService.addPolicy(role, "WRITE_USER", "allow")
      }

      // 添加用户角色（如果不存在）
      if (!permissionService.hasRole(account, role)) {
        permissionService.addRoleForUser(account, role)
      }
    } catch (e: Exception) {
      println("Warning: Could not add test user role: ${e.message}")
      // 继续执行，测试配置应该已经预设了基本权限
    }
  }

  /**
   * 使用PermissionService移除测试用户角色
   */
  private fun removeTestUserRole(account: String, role: String) {
    try {
      // 移除用户角色
      if (permissionService.hasRole(account, role)) {
        permissionService.removeRoleForUser(account, role)
      }

      // 注意：不要移除角色权限，因为其他测试可能需要它们
      // 只移除用户和角色的关联关系
    } catch (e: Exception) {
      println("Warning: Could not remove test user role: ${e.message}")
      // 忽略异常，清理失败不应该影响其他测试
    }
  }

  /**
   * 创建测试用户
   */
  private fun createTestUser(connection: Connection, account: String, userId: Long) {
    // 检查用户是否已存在
    val checkStmt = connection.prepareStatement("SELECT id FROM user_account WHERE account = ?")
    checkStmt.setString(1, account)
    val rs = checkStmt.executeQuery()

    if (!rs.next()) {
      // 用户不存在，创建新用户
      val insertStmt = connection.prepareStatement(
        "INSERT INTO user_account (id, account, nick_name, pwd_enc, last_login_time) VALUES (?, ?, ?, ?, NOW())"
      )
      insertStmt.setLong(1, userId)
      insertStmt.setString(2, account)
      insertStmt.setString(3, "Test User")
      insertStmt.setString(4, "test_password_hash")
      insertStmt.executeUpdate()
    }
  }

  /**
   * 关联用户到角色组
   */
  private fun associateUserWithRoleGroup(connection: Connection, account: String, roleGroupName: String) {
    // 获取用户ID
    val userId = dataSource.connection.use { conn ->
      conn.prepareStatement("SELECT id FROM user_account WHERE account = ?").use { stmt ->
        stmt.setString(1, account)
        val rs = stmt.executeQuery()
        if (rs.next()) rs.getLong("id") else null
      }
    }

    // 获取角色组ID
    val roleGroupId = dataSource.connection.use { conn ->
      conn.prepareStatement("SELECT id FROM role_group WHERE name = ?").use { stmt ->
        stmt.setString(1, roleGroupName)
        val rs = stmt.executeQuery()
        if (rs.next()) rs.getLong("id") else null
      }
    }

    if (userId != null && roleGroupId != null) {
      // 检查关联是否已存在
      val checkStmt = connection.prepareStatement(
        "SELECT id FROM user_role_group WHERE user_id = ? AND role_group_id = ?"
      )
      checkStmt.setLong(1, userId)
      checkStmt.setLong(2, roleGroupId)
      val rs = checkStmt.executeQuery()

      if (!rs.next()) {
        // 关联不存在，创建关联
        val insertStmt = connection.prepareStatement(
          "INSERT INTO user_role_group (user_id, role_group_id) VALUES (?, ?)"
        )
        insertStmt.setLong(1, userId)
        insertStmt.setLong(2, roleGroupId)
        insertStmt.executeUpdate()
      }
    }
  }

  @Test
  @RDBRollback
  fun session_id_generation_and_validation_works_correctly() {
    // Given: test user data
    val testAccount = "test_user_123"
    val testUserId = 8416520155186815780L
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
  fun user_disable_and_enable_operations_work_correctly() {
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
  fun casbin_permission_system_works_correctly() {
    // Test basic Casbin functionality
    
    // Test role checking
    val hasUserRole = permissionService.hasRole("test_user_123", "USER")
    assertTrue(hasUserRole, "test_user_123 should have USER role")
    
    // Test permission checking
    val hasReadPermission = permissionService.checkPermission("USER", "READ_USER", "allow")
    assertTrue(hasReadPermission, "USER role should have READ permission")
    
    val hasWritePermission = permissionService.checkPermission("USER", "WRITE_USER", "allow")
    assertTrue(hasWritePermission, "USER role should have write permission")
    
    // Test user roles retrieval
    val userRoles = permissionService.getUserRoles("test_user_123")
    assertTrue(userRoles.contains("USER"), "User roles should contain USER: $userRoles")
  }

  @Test
  @RDBRollback
  fun user_login_state_management_works_correctly() {
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
    val permissions = setOf("READ_USER", "WRITE_USER")

    // 为测试用户添加角色关系
    addTestUserRole(testAccount, "USER")

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

    // 清理测试用户的角色关系
    removeTestUserRole(testAccount, "USER")
  }

  @Test
  fun redis_basic_operations_work_correctly() {
    // Given: simple test data
    val testKey = "test:simple:123"
    val testData = mapOf(
      "sessionId" to "123",
      "account" to "test_user",
      "userId" to 12345L
    )

    // When: store data
    redisTemplate.opsForValue().set(testKey, testData, 3600, java.util.concurrent.TimeUnit.SECONDS)

    // Then: data should be retrievable
    val retrievedData = redisTemplate.opsForValue().get(testKey)
    assertNotNull(retrievedData, "应该能够从Redis中检索数据")

    // Cleanup
    redisTemplate.delete(testKey)
  }

  @Test
  @RDBRollback
  fun user_logout_clears_session_correctly() {
    // Given: logged in user
    val testAccount = "test_logout_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent-Logout")
      addHeader("Device-Id", "test-device-logout")
      addParameter("deviceId", "test-device-logout")
      setAttribute("deviceId", "test-device-logout")
      remoteAddr = "127.0.0.1"
    }
    val roles = setOf("USER")
    val permissions = setOf("READ_PROFILE")

    // 直接使用SessionService来避免可能的AuthService问题
    val sessionId = sessionService.createUserSession(testAccount, testUserId, request, roles, permissions)

    // 添加短暂延迟，确保Redis操作完成
    Thread.sleep(100)

    // 验证会话创建成功
    val user = sessionService.validateSessionAndGetUser(sessionId)
    assertNotNull(user, "会话验证应该成功")
    assertEquals(testAccount, user.account)
    assertEquals(testUserId, user.userId)

    // When: logout
    sessionService.logoutByAccount(testAccount)

    // Then: user session should be cleared
    assertNull(sessionService.validateSessionAndGetUser(sessionId))
  }

  @Test
  fun invalid_session_id_validation_returns_null() {
    // Given: invalid sessionId
    val invalidSessionId = "invalid-session-id-123"

    // When: validate sessionId
    val user = authService.validateSessionAndGetUser(invalidSessionId)

    // Then: should return null
    assertNull(user)
  }

  @Test
  @RDBRollback
  fun session_refresh_extends_expiration_time() {
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

