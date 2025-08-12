package com.tnmaster.application.service

import com.tnmaster.security.SessionData
import com.tnmaster.service.SessionService
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.TimeUnit
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

  @Resource
  lateinit var redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>

  @Test
  fun simple_redis_test() {
    // 简单的 Redis 测试
    val testKey = "test:simple:key"
    val testValue = "test-value"

    // 直接使用 RedisTemplate 进行测试
    redisTemplate.opsForValue().set(testKey, testValue)

    val retrievedValue = redisTemplate.opsForValue().get(testKey)
    println("Debug: stored=$testValue, retrieved=$retrievedValue")

    assertEquals(testValue, retrievedValue)
  }

  @Test
  fun session_id_generation_and_validation_works_correctly() {
    // Given: test user data
    val testAccount = "test_user_123"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val testRoles = setOf("USER")
    val testPermissions = setOf("READ_USER", "WRITE_USER")
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
      addHeader("X-Device-Id", "test-device-123")
      remoteAddr = "127.0.0.1"
    }

    // When: create session
    val sessionId = sessionService.createUserSession(testAccount, testUserId, request, testRoles, testPermissions)

    // Then: session should be valid
    assertNotNull(sessionId)
    assertTrue(sessionService.sessionExists(sessionId))

    // 添加短暂延迟，确保 Redis 操作完成
    Thread.sleep(100)

    // 添加调试信息：检查会话数据是否能直接获取
    val sessionData = sessionService.getSessionData(sessionId)
    println("Debug: sessionData = $sessionData")

    // 验证会话数据中存储的角色和权限
    assertNotNull(sessionData)
    assertEquals(testRoles, sessionData.roles)
    assertEquals(testPermissions, sessionData.permissions)

    val user = sessionService.validateSessionAndGetUser(sessionId)
    println("Debug: user = $user")
    assertNotNull(user)
    assertEquals(testAccount, user.account)
    assertEquals(testUserId, user.userId)
    // 注意：validateSessionAndGetUser会从权限服务重新获取角色和权限，而不是使用会话中存储的
    // 因此这里只验证用户基本信息，不验证角色和权限
  }

  @Test
  fun invalid_session_id_validation_returns_null() {
    // Given: invalid sessionId
    val invalidSessionId = "invalid-session-id-123"

    // When: validate sessionId
    val user = sessionService.validateSessionAndGetUser(invalidSessionId)

    // Then: should return null
    assertNull(user)
  }

  @Test
  fun session_refresh_extends_expiration_time() {
    // Given: active session
    val testAccount = "test_refresh_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
      addHeader("X-Device-Id", "test-device-456")
      remoteAddr = "127.0.0.1"
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
  fun user_logout_clears_session_correctly() {
    // Given: logged in user
    val testAccount = "test_logout_user"
    val testUserId = Random.nextLong(1, Long.MAX_VALUE)
    val request = MockHttpServletRequest().apply {
      addHeader("User-Agent", "Test-Agent")
      addHeader("X-Device-Id", "test-device-789")
      remoteAddr = "127.0.0.1"
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

  @Test
  fun test_simple_object_serialization_to_redis() {
    // Given: 使用Map来测试Redis序列化功能，避免自定义数据类的类型问题
    val testData = mapOf(
      "id" to "test-123",
      "name" to "测试数据",
      "count" to 42
    )
    val testKey = "test:simple:data"

    // When: 存储到Redis
    redisTemplate.opsForValue().set(testKey, testData, 60, TimeUnit.SECONDS)

    // Then: 立即从Redis读取
    val retrievedData = redisTemplate.opsForValue().get(testKey) as? Map<*, *>

    // 验证数据是否正确
    assertNotNull(retrievedData, "从Redis读取的数据不应该为null")
    assertEquals(testData["id"], retrievedData["id"])
    assertEquals(testData["name"], retrievedData["name"])
    assertEquals(testData["count"], retrievedData["count"])

    // 清理测试数据
    redisTemplate.delete(testKey)
  }

  @Test
  fun test_string_serialization_to_redis() {
    // Given: 简单的字符串数据
    val testValue = "test-string-value"
    val testKey = "test:string:data"

    // When: 存储到Redis
    redisTemplate.opsForValue().set(testKey, testValue, 60, TimeUnit.SECONDS)

    // Then: 立即从Redis读取
    val retrievedValue = redisTemplate.opsForValue().get(testKey) as? String

    // 验证数据是否正确
    assertNotNull(retrievedValue, "从Redis读取的字符串不应该为null")
    assertEquals(testValue, retrievedValue)

    // 清理测试数据
    redisTemplate.delete(testKey)
  }

  @Test
  fun test_objectmapper_sessiondata_serialization() {
    // Given: 直接测试ObjectMapper的SessionData序列化
    val testSessionData = SessionData(
      sessionId = "test-session-123",
      account = "testuser",
      userId = 1L,
      deviceId = "test-device",
      loginIpAddr = "127.0.0.1",
      loginTime = datetime.now(),
      roles = setOf("USER"),
      permissions = setOf("READ"),
      expireTime = datetime.now().plusSeconds(3600)
    )

    // 获取RedisTemplate使用的序列化器
    val valueSerializer = redisTemplate.valueSerializer
    assertNotNull(valueSerializer, "ValueSerializer不应该为null")

    // 测试序列化 - 使用Any类型避免泛型问题
    @Suppress("UNCHECKED_CAST")
    val anySerializer = valueSerializer as org.springframework.data.redis.serializer.RedisSerializer<Any>
    val serialized = anySerializer.serialize(testSessionData)
    println("Debug: 序列化后的JSON = ${String(serialized!!)}")

    // 测试反序列化
    val deserialized = anySerializer.deserialize(serialized)
    println("Debug: 反序列化后的数据 = $deserialized")
    println("Debug: 反序列化后的数据类型 = ${deserialized?.javaClass}")

    // 验证反序列化结果
    assertNotNull(deserialized, "反序列化结果不应该为null")
    assertTrue(deserialized is SessionData, "反序列化结果应该是SessionData类型")
    val sessionData = deserialized as SessionData
    assertEquals(testSessionData.sessionId, sessionData.sessionId)
    assertEquals(testSessionData.account, sessionData.account)
  }

  @Test
  fun test_datetime_serialization_to_redis() {
    // Given: 测试datetime类型的序列化
    val testDatetime = datetime.now()
    val testKey = "test:datetime:data"

    // When: 存储到Redis
    redisTemplate.opsForValue().set(testKey, testDatetime, 60, TimeUnit.SECONDS)

    // Then: 立即从Redis读取
    val rawData = redisTemplate.opsForValue().get(testKey)
    println("Debug: 从Redis读取的原始datetime数据 = $rawData")
    println("Debug: 原始datetime数据类型 = ${rawData?.javaClass}")

    val retrievedDatetime = rawData as? datetime

    // 验证数据是否正确
    assertNotNull(retrievedDatetime, "从Redis读取的datetime不应该为null")
    assertEquals(testDatetime, retrievedDatetime)

    // 清理测试数据
    redisTemplate.delete(testKey)
  }

  @Test
  fun test_session_data_serialization_to_redis() {
    // Given: 创建一个简单的SessionData对象
    val testSessionData = SessionData(
      sessionId = "test-session-123",
      account = "testuser",
      userId = 1L,
      deviceId = "test-device",
      loginIpAddr = "127.0.0.1",
      loginTime = datetime.now(),
      roles = setOf("USER"),
      permissions = setOf("READ"),
      expireTime = datetime.now().plusSeconds(3600)
    )

    val testKey = "test:session:data"

    println("Debug: 准备存储的SessionData = $testSessionData")

    // When: 存储到Redis
    try {
      redisTemplate.opsForValue().set(testKey, testSessionData, 60, TimeUnit.SECONDS)
      println("Debug: 成功存储到Redis")

      // 检查Redis中存储的原始JSON数据
      val rawBytes = redisTemplate.connectionFactory?.connection?.get(testKey.toByteArray())
      if (rawBytes != null) {
        println("Debug: Redis中存储的原始JSON = ${String(rawBytes)}")
      }
    } catch (e: Exception) {
      println("Debug: 存储到Redis失败: ${e.message}")
      e.printStackTrace()
      throw e
    }

    // Then: 立即从Redis读取
    val retrievedData = try {
      val rawData = redisTemplate.opsForValue().get(testKey)
      println("Debug: 从Redis读取的原始数据 = $rawData")
      println("Debug: 原始数据类型 = ${rawData?.javaClass}")
      rawData as? SessionData
    } catch (e: Exception) {
      println("Debug: 从Redis读取失败: ${e.message}")
      e.printStackTrace()
      null
    }

    println("Debug: 转换后的SessionData = $retrievedData")

    // 验证数据是否正确
    assertNotNull(retrievedData, "从Redis读取的数据不应该为null")
    assertEquals(testSessionData.sessionId, retrievedData.sessionId)
    assertEquals(testSessionData.account, retrievedData.account)
    assertEquals(testSessionData.userId, retrievedData.userId)

    // 清理测试数据
    redisTemplate.delete(testKey)
  }
}

