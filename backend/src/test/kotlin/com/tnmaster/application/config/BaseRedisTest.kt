package com.tnmaster.application.config

import io.github.truenine.composeserver.testtoolkit.testcontainers.ICacheRedisContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Redis相关测试的基类
 * 提供统一的Redis测试环境配置和清理机制
 * 同时启动PostgreSQL容器以支持Flyway迁移
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfiguration::class)
@Testcontainers
abstract class BaseRedisTest : ICacheRedisContainer, IDatabasePostgresqlContainer {

  @Autowired
  protected lateinit var redisTemplate: RedisTemplate<String, Any>

  @BeforeEach
  fun setupRedisTest() {
    // 清理Redis缓存，确保测试隔离
    try {
      redisTemplate.connectionFactory?.connection?.use { connection ->
        connection.serverCommands().flushAll()
      }
    } catch (e: Exception) {
      // 如果清理失败，记录日志但不影响测试
      println("Warning: Failed to flush Redis cache before test: ${e.message}")
    }
  }

  @AfterEach
  fun cleanupRedisTest() {
    // 测试后清理，确保不影响其他测试
    try {
      redisTemplate.connectionFactory?.connection?.use { connection ->
        connection.serverCommands().flushAll()
      }
    } catch (e: Exception) {
      // 如果清理失败，记录日志但不影响测试
      println("Warning: Failed to flush Redis cache after test: ${e.message}")
    }
  }

  /**
   * 获取Redis中存储的原始数据，用于调试
   */
  protected fun getRawRedisData(key: String): Any? {
    return try {
      redisTemplate.opsForValue().get(key)
    } catch (e: Exception) {
      println("Error getting raw Redis data for key '$key': ${e.message}")
      null
    }
  }

  /**
   * 检查Redis键是否存在
   */
  protected fun redisKeyExists(key: String): Boolean {
    return try {
      redisTemplate.hasKey(key)
    } catch (e: Exception) {
      println("Error checking Redis key existence for '$key': ${e.message}")
      false
    }
  }

  /**
   * 删除Redis键
   */
  protected fun deleteRedisKey(key: String): Boolean {
    return try {
      redisTemplate.delete(key)
    } catch (e: Exception) {
      println("Error deleting Redis key '$key': ${e.message}")
      false
    }
  }
}
