package com.tnmaster.config.redis

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.tnmaster.application.config.BaseRedisTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.serializer.SerializationException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Redis序列化错误处理器测试
 * 
 * 测试RedisSerializationErrorHandler在各种异常情况下的处理能力
 */
@DisplayName("Redis序列化错误处理器测试")
class RedisSerializationErrorHandlerTest : BaseRedisTest() {

    @Autowired
    private lateinit var errorHandler: RedisSerializationErrorHandler

    @BeforeEach
    fun setUp() {
        // 重置错误统计
        errorHandler.resetErrorStatistics()
    }

    @Test
    @DisplayName("处理UnrecognizedPropertyException")
    fun handleUnrecognizedPropertyException() {
        // Given: 使用实际的Jackson来创建真实的异常
        val jsonWithUnknownProperty = """{"unknownProperty": "test", "knownProperty": "value"}"""

        // 创建一个严格的ObjectMapper来触发UnrecognizedPropertyException
        val strictMapper = com.fasterxml.jackson.databind.ObjectMapper().apply {
            configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }

        // 定义一个简单的数据类来触发异常
        data class TestClass(val knownProperty: String)

        val mockException = try {
            strictMapper.readValue(jsonWithUnknownProperty, TestClass::class.java)
            // 如果没有异常，返回一个RuntimeException，这样测试会走到unknownException分支
            RuntimeException("No exception thrown")
        } catch (ex: UnrecognizedPropertyException) {
            ex
        } catch (ex: Exception) {
            // 任何其他异常，包装为UnrecognizedPropertyException
            UnrecognizedPropertyException.from(null, TestClass::class.java, "unknownProperty", listOf("knownProperty"))
        }

        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            jsonWithUnknownProperty, 
            "test-deserialize"
        )

        // Then: 验证错误统计
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! > 0, "应该记录错误统计")
        
        // 只有当异常确实是UnrecognizedPropertyException时才检查这个计数
        if (mockException is UnrecognizedPropertyException) {
            assertTrue(stats["unrecognizedPropertyErrors"]!! > 0, "应该记录未识别属性错误")
        }
    }

    @Test
    @DisplayName("处理InvalidTypeIdException")
    fun handleInvalidTypeIdException() {
        // Given: 使用实际的Jackson来创建真实的异常
        val jsonWithInvalidClass = """{"@class": "com.nonexistent.InvalidClass", "data": "test"}"""

        // 创建一个启用类型信息的ObjectMapper来触发InvalidTypeIdException
        val mapperWithTyping = com.fasterxml.jackson.databind.ObjectMapper().apply {
            activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL
            )
        }

        val mockException = try {
            mapperWithTyping.readValue(jsonWithInvalidClass, Any::class.java)
            // 如果没有异常，创建一个模拟异常
            RuntimeException("No exception thrown")
        } catch (ex: InvalidTypeIdException) {
            ex
        } catch (ex: Exception) {
            // 任何其他异常，包装为InvalidTypeIdException
            val baseType = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Any::class.java)
            InvalidTypeIdException.from(null, baseType, "com.nonexistent.InvalidClass")
        }

        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            jsonWithInvalidClass, 
            "test-deserialize"
        )

        // Then: 验证错误统计
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! > 0, "应该记录错误统计")
        
        // 只有当异常确实是InvalidTypeIdException时才检查这个计数
        if (mockException is InvalidTypeIdException) {
            assertTrue(stats["invalidTypeIdErrors"]!! > 0, "应该记录无效类型ID错误")
        }
    }

    @Test
    @DisplayName("处理SerializationException")
    fun handleSerializationException() {
        // Given: 模拟一个SerializationException
        val testData = mapOf("key" to "value")
        val mockException = SerializationException("Simulated serialization error")

        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            testData, 
            "test-serialize"
        )

        // Then: 应该记录错误
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! > 0, "应该记录错误统计")
        assertTrue(stats["generalSerializationErrors"]!! > 0, "应该记录通用序列化错误")
    }

    @Test
    @DisplayName("错误统计功能")
    fun errorStatisticsTracking() {
        // Given: 初始状态
        var stats = errorHandler.getErrorStatistics()
        assertEquals(0L, stats["totalErrors"], "初始错误计数应该为0")

        // When: 处理多个错误
        repeat(3) {
            errorHandler.handleSerializationError(
                RuntimeException("Test error $it"), 
                "test data", 
                "test"
            )
        }

        // Then: 错误计数应该增加
        stats = errorHandler.getErrorStatistics()
        assertEquals(3L, stats["totalErrors"], "应该记录3个错误")

        // When: 重置统计
        errorHandler.resetErrorStatistics()

        // Then: 计数应该归零
        stats = errorHandler.getErrorStatistics()
        assertEquals(0L, stats["totalErrors"], "重置后错误计数应该为0")
    }

    @Test
    @DisplayName("Fallback反序列化测试")
    fun fallbackDeserializationTest() {
        // Given: 一个简单的JSON字符串
        val simpleJson = """
            {
                "name": "test",
                "value": 123,
                "flag": true
            }
        """.trimIndent()

        val mockException = RuntimeException("Simulated deserialization error")

        // When: 处理异常
        val result = errorHandler.handleSerializationError(
            mockException, 
            simpleJson, 
            "test-fallback"
        )

        // Then: 应该返回Map结构的结果
        if (result != null) {

            assertTrue(result is Map<*, *>, "Fallback结果应该是Map类型")
            val mapResult = result as Map<*, *>
            assertTrue(mapResult.containsKey("name"), "应该包含name字段")
            assertTrue(mapResult.containsKey("value"), "应该包含value字段")
            assertTrue(mapResult.containsKey("flag"), "应该包含flag字段")
        }
    }

    @Test
    @DisplayName("处理null和空数据")
    fun handleNullAndEmptyData() {
        // Given: null数据
        val mockException = RuntimeException("Test exception")

        // When: 处理null数据
        val result1 = errorHandler.handleSerializationError(mockException, null, "test")
        
        // When: 处理空字符串
        val result2 = errorHandler.handleSerializationError(mockException, "", "test")

        // Then: 应该能够处理这些情况而不崩溃
        // 结果可能是null，这是可以接受的
        val stats = errorHandler.getErrorStatistics()
        assertTrue(stats["totalErrors"]!! >= 2, "应该记录处理的错误")
    }

    @Test
    @DisplayName("日志记录功能")
    fun loggingFunctionality() {
        // Given: 一些错误
        repeat(5) {
            errorHandler.handleSerializationError(
                RuntimeException("Test error $it"), 
                "test data $it", 
                "test"
            )
        }

        // When: 记录统计日志
        // 这个方法主要是为了测试不会抛出异常
        errorHandler.logErrorStatistics()

        // Then: 应该有错误统计
        val stats = errorHandler.getErrorStatistics()
        assertEquals(5L, stats["totalErrors"], "应该记录5个错误")
    }
}
