package com.tnmaster.config.redis

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * 集合序列化问题修复测试
 * 专门测试集合序列化格式修复功能
 */
class CollectionSerializationFixTest {

    @Test
    fun `测试集合序列化格式修复功能`() {
        val errorHandler = RedisSerializationErrorHandler()
        
        // 模拟problematic JSON - SingletonSet格式
        val problematicJson = """
            {
                "sessionId": "test-session-123",
                "account": "testuser", 
                "userId": 1,
                "deviceId": "device-456",
                "loginIpAddr": "192.168.1.100",
                "loginTime": "2025-08-13T15:00:00.000000000",
                "roles": ["java.util.Collections${'$'}SingletonSet", ["USER"]],
                "permissions": ["java.util.Collections${'$'}SingletonSet", ["READ_PROFILE"]],
                "expireTime": "2025-08-14T15:00:00.000000000",
                "disabled": false
            }
        """.trimIndent()
        
        println("原始问题JSON: $problematicJson")
        
        // 创建模拟异常
        val mockEx = Exception("Test serialization error")
        
        // 使用错误处理器处理
        val result = errorHandler.handleSerializationError(mockEx, problematicJson, "deserialize")
        
        assertNotNull(result, "错误处理器应该能够处理集合序列化问题")
        println("处理结果: $result")
        
        // 验证结果包含正确的字段
        if (result is Map<*, *>) {
            assertEquals("test-session-123", result["sessionId"])
            assertEquals("testuser", result["account"])
            
            // 验证集合字段被正确处理
            val roles = result["roles"] as? List<*>
            assertNotNull(roles, "roles字段应该被正确处理")
            assertEquals(listOf("USER"), roles)
            
            val permissions = result["permissions"] as? List<*>  
            assertNotNull(permissions, "permissions字段应该被正确处理")
            assertEquals(listOf("READ_PROFILE"), permissions)
        }
    }
}