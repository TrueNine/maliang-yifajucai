package com.tnmaster.config.redis

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import io.github.truenine.composeserver.logger
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.stereotype.Component

/**
 * Redis序列化错误处理器
 * 
 * 处理Redis序列化/反序列化过程中的各种异常情况，提供错误恢复策略和详细的错误日志记录。
 * 主要处理以下异常类型：
 * - UnrecognizedPropertyException: 未识别的属性异常
 * - InvalidTypeIdException: 无效的类型ID异常
 * - SerializationException: 通用序列化异常
 * 
 * @author TrueNine
 * @since 2025-01-12
 */
@Component
class RedisSerializationErrorHandler {

    companion object {
        private val log = logger<RedisSerializationErrorHandler>()
        
        // 错误统计计数器
        private var unrecognizedPropertyCount = 0L
        private var invalidTypeIdCount = 0L
        private var generalSerializationErrorCount = 0L
    }

    /**
     * 处理序列化异常的主要入口点
     * 
     * @param ex 序列化异常
     * @param originalData 原始数据（用于日志记录）
     * @param operation 操作类型（序列化或反序列化）
     * @return 处理后的结果，如果无法恢复则返回null
     */
    fun handleSerializationError(
        ex: Exception, 
        originalData: Any? = null, 
        operation: String = "unknown"
    ): Any? {
        return when (ex) {
            is UnrecognizedPropertyException -> handleUnrecognizedPropertyException(ex, originalData, operation)
            is InvalidTypeIdException -> handleInvalidTypeIdException(ex, originalData, operation)
            is SerializationException -> handleGeneralSerializationException(ex, originalData, operation)
            is JsonProcessingException -> handleJsonProcessingException(ex, originalData, operation)
            is JsonMappingException -> handleJsonMappingException(ex, originalData, operation)
            else -> handleUnknownException(ex, originalData, operation)
        }
    }

    /**
     * 处理未识别属性异常
     * 
     * 当Jackson遇到JSON中存在但目标类中不存在的属性时抛出此异常。
     * 处理策略：
     * 1. 记录详细的错误信息
     * 2. 尝试忽略未知属性重新反序列化
     * 3. 如果仍然失败，返回null
     */
    private fun handleUnrecognizedPropertyException(
        ex: UnrecognizedPropertyException, 
        originalData: Any?, 
        operation: String
    ): Any? {
        unrecognizedPropertyCount++
        
        log.warn(
            "Redis序列化遇到未识别属性异常 [操作: {}, 计数: {}] - " +
            "属性名: '{}', 目标类: {}, 已知属性: {}", 
            operation,
            unrecognizedPropertyCount,
            ex.propertyName,
            ex.referringClass?.simpleName ?: "Unknown",
            ex.knownPropertyIds?.joinToString(", ") ?: "None"
        )
        
        // 记录原始数据用于调试
        if (originalData != null) {
            log.debug("原始数据: {}", originalData)
        }
        
        // 尝试使用容错的ObjectMapper重新处理
        return attemptFallbackDeserialization(ex, originalData, "UnrecognizedProperty")
    }

    /**
     * 处理无效类型ID异常
     * 
     * 当Jackson无法解析@class类型信息时抛出此异常。
     * 处理策略：
     * 1. 记录类型ID相关信息
     * 2. 尝试使用默认类型进行反序列化
     * 3. 提供类型映射的fallback策略
     */
    private fun handleInvalidTypeIdException(
        ex: InvalidTypeIdException, 
        originalData: Any?, 
        operation: String
    ): Any? {
        invalidTypeIdCount++
        
        log.warn(
            "Redis序列化遇到无效类型ID异常 [操作: {}, 计数: {}] - " +
            "类型ID: '{}', 基础类型: {}", 
            operation,
            invalidTypeIdCount,
            ex.typeId,
            ex.baseType?.rawClass?.simpleName ?: "Unknown"
        )
        
        // 记录原始数据用于调试
        if (originalData != null) {
            log.debug("原始数据: {}", originalData)
        }
        
        // 尝试类型映射fallback
        return attemptTypeIdFallback(ex, originalData)
    }

    /**
     * 处理通用序列化异常
     */
    private fun handleGeneralSerializationException(
        ex: SerializationException, 
        originalData: Any?, 
        operation: String
    ): Any? {
        generalSerializationErrorCount++
        
        log.error(
            "Redis序列化遇到通用异常 [操作: {}, 计数: {}] - 消息: {}", 
            operation,
            generalSerializationErrorCount,
            ex.message,
            ex
        )
        
        // 检查是否是由其他已知异常引起的
        val cause = ex.cause
        if (cause != null) {
            log.debug("尝试处理根本原因异常: {}", cause.javaClass.simpleName)
            return handleSerializationError(cause, originalData, "$operation-nested")
        }
        
        return null
    }

    /**
     * 处理JSON处理异常
     */
    private fun handleJsonProcessingException(
        ex: JsonProcessingException, 
        originalData: Any?, 
        operation: String
    ): Any? {
        log.warn(
            "Redis序列化遇到JSON处理异常 [操作: {}] - 位置: {}, 消息: {}", 
            operation,
            ex.location?.toString() ?: "Unknown",
            ex.originalMessage
        )
        
        return attemptFallbackDeserialization(ex, originalData, "JsonProcessing")
    }

    /**
     * 处理JSON映射异常
     */
    private fun handleJsonMappingException(
        ex: JsonMappingException, 
        originalData: Any?, 
        operation: String
    ): Any? {
        log.warn(
            "Redis序列化遇到JSON映射异常 [操作: {}] - 路径: {}, 消息: {}", 
            operation,
            ex.pathReference ?: "Unknown",
            ex.originalMessage
        )
        
        return attemptFallbackDeserialization(ex, originalData, "JsonMapping")
    }

    /**
     * 处理未知异常
     */
    private fun handleUnknownException(
        ex: Exception, 
        originalData: Any?, 
        operation: String
    ): Any? {
        log.error(
            "Redis序列化遇到未知异常 [操作: {}] - 类型: {}, 消息: {}", 
            operation,
            ex.javaClass.simpleName,
            ex.message,
            ex
        )
        
        return null
    }

    /**
     * 尝试使用容错的ObjectMapper进行fallback反序列化
     */
    private fun attemptFallbackDeserialization(
        ex: Exception, 
        originalData: Any?, 
        errorType: String
    ): Any? {
        if (originalData !is String && originalData !is ByteArray) {
            log.debug("原始数据不是字符串或字节数组，无法进行fallback反序列化")
            return null
        }
        
        try {
            // 创建一个更宽松的ObjectMapper用于fallback
            val fallbackMapper = createFallbackObjectMapper()
            
            val jsonString = when (originalData) {
                is String -> originalData
                is ByteArray -> String(originalData)
                else -> return null
            }
            
            log.debug("尝试使用fallback ObjectMapper进行{}反序列化", errorType)
            
            // 尝试反序列化为通用的Map结构
            val result = fallbackMapper.readValue(jsonString, Map::class.java)
            log.info("Fallback反序列化成功，返回Map结构: {}", result.keys)
            return result
            
        } catch (fallbackEx: Exception) {
            log.warn("Fallback反序列化也失败了: {}", fallbackEx.message)
            return null
        }
    }

    /**
     * 尝试类型ID的fallback处理
     */
    private fun attemptTypeIdFallback(ex: InvalidTypeIdException, originalData: Any?): Any? {
        val typeId = ex.typeId
        val baseType = ex.baseType
        
        // 常见的类型映射fallback
        val fallbackTypeMapping = mapOf(
            "com.tnmaster.entities.ApiCallRecordDraft" to "com.tnmaster.entities.ApiCallRecord",
            "com.tnmaster.security.SessionData" to Map::class.java.name,
            // 可以根据需要添加更多映射
        )
        
        val fallbackType = fallbackTypeMapping[typeId]
        if (fallbackType != null) {
            log.info("尝试使用fallback类型映射: {} -> {}", typeId, fallbackType)
            
            try {
                val fallbackMapper = createFallbackObjectMapper()
                val jsonString = when (originalData) {
                    is String -> originalData
                    is ByteArray -> String(originalData)
                    else -> return null
                }
                
                // 移除@class属性并尝试反序列化
                val jsonWithoutClass = removeClassProperty(jsonString)
                return fallbackMapper.readValue(jsonWithoutClass, Map::class.java)
                
            } catch (fallbackEx: Exception) {
                log.warn("类型ID fallback失败: {}", fallbackEx.message)
            }
        }
        
        return attemptFallbackDeserialization(ex, originalData, "InvalidTypeId")
    }

    /**
     * 创建用于fallback的宽松ObjectMapper
     */
    private fun createFallbackObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // 配置为最宽松的反序列化设置
            configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            configure(com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            
            // 禁用类型信息处理
            deactivateDefaultTyping()
        }
    }

    /**
     * 从JSON字符串中移除@class属性
     */
    private fun removeClassProperty(jsonString: String): String {
        return jsonString.replace(Regex(""""@class"\s*:\s*"[^"]*"\s*,?\s*"""), "")
            .replace(Regex(",\s*}"), "}")  // 清理可能留下的尾随逗号
    }

    /**
     * 获取错误统计信息
     */
    fun getErrorStatistics(): Map<String, Long> {
        return mapOf(
            "unrecognizedPropertyErrors" to unrecognizedPropertyCount,
            "invalidTypeIdErrors" to invalidTypeIdCount,
            "generalSerializationErrors" to generalSerializationErrorCount,
            "totalErrors" to (unrecognizedPropertyCount + invalidTypeIdCount + generalSerializationErrorCount)
        )
    }

    /**
     * 重置错误统计计数器
     */
    fun resetErrorStatistics() {
        unrecognizedPropertyCount = 0L
        invalidTypeIdCount = 0L
        generalSerializationErrorCount = 0L
        log.info("Redis序列化错误统计计数器已重置")
    }

    /**
     * 记录错误统计摘要
     */
    fun logErrorStatistics() {
        val stats = getErrorStatistics()
        log.info(
            "Redis序列化错误统计摘要 - " +
            "未识别属性: {}, 无效类型ID: {}, 通用序列化错误: {}, 总计: {}",
            stats["unrecognizedPropertyErrors"],
            stats["invalidTypeIdErrors"], 
            stats["generalSerializationErrors"],
            stats["totalErrors"]
        )
    }
}
