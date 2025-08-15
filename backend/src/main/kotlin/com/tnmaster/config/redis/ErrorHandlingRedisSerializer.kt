package com.tnmaster.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.truenine.composeserver.logger
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.SerializationException

/**
 * 带错误处理的Redis序列化器
 *
 * 扩展GenericJackson2JsonRedisSerializer，集成RedisSerializationErrorHandler
 * 来处理序列化/反序列化过程中的各种异常情况。
 *
 * @author TrueNine
 * @since 2025-01-12
 */
class ErrorHandlingRedisSerializer(
  private val objectMapper: ObjectMapper,
  private val errorHandler: RedisSerializationErrorHandler,
) : GenericJackson2JsonRedisSerializer(objectMapper) {

  companion object {
    @JvmStatic
    private val log = logger<ErrorHandlingRedisSerializer>()
  }

  /**
   * 序列化对象到字节数组
   *
   * @param source 要序列化的对象
   * @return 序列化后的字节数组，如果序列化失败则返回null
   */
  override fun serialize(source: Any?): ByteArray {
    if (source == null) {
      return ByteArray(0)
    }

    try {
      val result = super.serialize(source)
      log.debug("Redis序列化成功 - 对象类型: {}, 字节数: {}", source.javaClass.simpleName, result.size)
      return result
    } catch (ex: Exception) {
      log.warn("Redis序列化失败，尝试错误处理 - 对象类型: {}", source.javaClass.simpleName, ex)

      // 使用错误处理器处理异常
      val handledResult = errorHandler.handleSerializationError(ex, source, "serialize")

      if (handledResult != null) {
        try {
          // 尝试序列化处理后的结果
          val result = super.serialize(handledResult)
          log.info(
            "Redis序列化错误处理成功 - 原始类型: {}, 处理后类型: {}",
            source.javaClass.simpleName, handledResult.javaClass.simpleName
          )
          return result
        } catch (retryEx: Exception) {
          log.error("重试序列化也失败了", retryEx)
        }
      }

      // 如果错误处理也失败，记录错误并抛出异常
      log.error("Redis序列化完全失败，对象: {}", source, ex)
      throw SerializationException("无法序列化对象: ${source.javaClass.simpleName}", ex)
    }
  }

  /**
   * 从字节数组反序列化对象
   *
   * @param source 要反序列化的字节数组
   * @return 反序列化后的对象，如果反序列化失败则返回null
   */
  override fun deserialize(source: ByteArray?): Any? {
    if (source == null || source.isEmpty()) {
      return null
    }

    try {
      val result = super.deserialize(source)
      log.debug("Redis反序列化成功 - 结果类型: {}", result?.javaClass?.simpleName ?: "null")
      return result
    } catch (ex: Exception) {
      val jsonString = String(source)
      log.warn("Redis反序列化失败，尝试错误处理 - JSON长度: {}", jsonString.length, ex)
      log.debug("失败的JSON内容: {}", jsonString)

      // 使用错误处理器处理异常
      val handledResult = errorHandler.handleSerializationError(ex, jsonString, "deserialize")

      if (handledResult != null) {
        log.info("错误处理器成功处理了反序列化异常，返回类型: {}", handledResult.javaClass.simpleName)

        // 尝试将Map转换回原始的SessionData类型
        val finalResult = attemptMapToSessionDataConversion(handledResult, jsonString)
        return finalResult ?: handledResult
      }

      // 如果错误处理也失败，记录错误但不抛出异常，返回null
      // 在测试环境中，这种行为可能导致测试失败，但在生产环境中可以避免系统崩溃
      log.error("Redis反序列化完全失败，JSON: {}", jsonString, ex)
      return null
    }
  }

  /**
   * 获取错误处理器的统计信息
   */
  fun getErrorStatistics(): Map<String, Long> {
    return errorHandler.getErrorStatistics()
  }

  /**
   * 重置错误统计
   */
  fun resetErrorStatistics() {
    errorHandler.resetErrorStatistics()
  }

  /**
   * 记录错误统计
   */
  fun logErrorStatistics() {
    errorHandler.logErrorStatistics()
  }

  /**
   * 尝试将Map转换回SessionData类型
   */
  private fun attemptMapToSessionDataConversion(result: Any, originalJson: String): Any? {
    if (result !is Map<*, *>) {
      return result
    }

    // 检查JSON中是否有SessionData的特征字段
    if (!originalJson.contains("sessionId") || !originalJson.contains("account")) {
      return result
    }

    try {
      log.debug("尝试将Map转换为SessionData")

      // 使用ObjectMapper将Map重新序列化为JSON，然后反序列化为SessionData
      val jsonBytes = objectMapper.writeValueAsBytes(result)
      val sessionData = objectMapper.readValue(jsonBytes, com.tnmaster.security.SessionData::class.java)

      log.info("成功将Map转换为SessionData: sessionId={}", sessionData.sessionId)
      return sessionData

    } catch (ex: Exception) {
      log.debug("Map到SessionData转换失败: {}", ex.message)
      return result
    }
  }
}
