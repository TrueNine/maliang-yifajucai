package com.tnmaster.config.redis

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.serializer.SerializationException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Redis序列化错误处理器的单元测试
 * 不依赖Spring上下文，直接测试核心逻辑
 */
class RedisSerializationErrorHandlerUnitTest {

    private lateinit var errorHandler: RedisSerializationErrorHandler

    @BeforeEach
    fun setUp() {
        errorHandler = RedisSerializationErrorHandler()
        errorHandler.resetErrorStatistics()
    }

    @Test
    fun `处理RuntimeException应该增加计数`() {
        // Given: 简单的RuntimeException
        val mockException = RuntimeException("Test runtime exception")
        val testData = "test data"
        
        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            testData, 
            "test-operation"
        )

        // Then: 验证错误统计（RuntimeException会走到unknownException处理器，增加generalSerializationErrors计数）
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! > 0, "应该记录错误统计")
        assertTrue(stats["generalSerializationErrors"]!! > 0, "应该记录通用序列化错误")
    }

    @Test
    fun `处理SerializationException应该增加计数`() {
        // Given: 创建SerializationException
        val testData = mapOf("key" to "value")
        val mockException = SerializationException("Simulated serialization error")

        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            testData, 
            "test-serialize"
        )

        // Then: 验证错误统计
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! > 0, "应该记录错误统计")
        assertTrue(stats["generalSerializationErrors"]!! > 0, "应该记录通用序列化错误")
    }

    @Test
    fun `错误统计功能应该正常工作`() {
        // Given: 初始状态
        var stats = errorHandler.getErrorStatistics()
        assertEquals(0L, stats["totalErrors"], "初始错误计数应该为0")

        // When: 处理多个错误
        repeat(3) {
            errorHandler.handleSerializationError(
                SerializationException("Test error $it"), 
                "test data", 
                "test"
            )
        }

        // Then: 错误计数应该增加
        stats = errorHandler.getErrorStatistics()
        assertEquals(3L, stats["totalErrors"], "应该记录3个错误")
        assertEquals(3L, stats["generalSerializationErrors"], "应该记录3个通用序列化错误")

        // When: 重置统计
        errorHandler.resetErrorStatistics()

        // Then: 计数应该归零
        stats = errorHandler.getErrorStatistics()
        assertEquals(0L, stats["totalErrors"], "重置后错误计数应该为0")
    }

    @Test
    fun `处理null数据应该不崩溃`() {
        // Given: null数据
        val mockException = RuntimeException("Test exception")

        // When: 处理null数据
        val result1 = errorHandler.handleSerializationError(mockException, null, "test")
        
        // When: 处理空字符串
        val result2 = errorHandler.handleSerializationError(mockException, "", "test")

        // Then: 应该能够处理这些情况而不崩溃
        val stats = errorHandler.getErrorStatistics()
        assertEquals(2L, stats["totalErrors"], "应该记录2个错误")
    }

    @Test
    fun `日志统计功能应该不崩溃`() {
        // Given: 一些错误
        repeat(5) {
            errorHandler.handleSerializationError(
                RuntimeException("Test error $it"), 
                "test data $it", 
                "test"
            )
        }

        // When: 记录统计日志（主要验证不会抛出异常）
        errorHandler.logErrorStatistics()

        // Then: 应该有错误统计
        val stats = errorHandler.getErrorStatistics()
        assertEquals(5L, stats["totalErrors"], "应该记录5个错误")
    }
}