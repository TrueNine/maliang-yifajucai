package com.tnmaster.application.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.tnmaster.config.DatetimeDeserializer
import com.tnmaster.config.DatetimeSerializer
import com.tnmaster.config.redis.ErrorHandlingRedisSerializer
import com.tnmaster.config.redis.RedisSerializationErrorHandler
import com.tnmaster.entities.ApiCallRecord
import io.github.truenine.composeserver.datetime
import org.babyfish.jimmer.jackson.ImmutableModule
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * 标准化的测试Redis配置
 * 解决Redis序列化问题，特别是@class类型信息处理
 */
@TestConfiguration
class TestRedisConfiguration {

    /**
     * Redis序列化错误处理器
     */
    @Bean
    fun redisSerializationErrorHandler(): RedisSerializationErrorHandler {
        return RedisSerializationErrorHandler()
    }

    /**
     * 创建统一的ObjectMapper用于Redis序列化
     * 支持Kotlin数据类、Jimmer实体、datetime类型和多态类型
     * Enhanced configuration for better Kotlin data class and builder support
     */
    @Bean
    @Primary
    fun testRedisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // Enhanced Kotlin module configuration for better data class and builder support
            val kotlinModule = KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
            
            // Register modules for comprehensive type support
            registerModule(kotlinModule)
            registerModule(JavaTimeModule())
            registerModule(ImmutableModule())
            
            // 创建自定义模块来处理datetime类型
            val datetimeModule = SimpleModule("TestDatetimeModule")
            datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
            datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
            registerModule(datetimeModule)
            
            // Enhanced deserialization features for robust error handling
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false)
            configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, false)
            configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, false)
            
            // Enhanced serialization features for consistent output
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false)
            configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
            
            // 创建自定义验证器以排除datetime类型的多态处理
            val customValidator = object : com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator() {
                override fun validateBaseType(config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?, baseType: com.fasterxml.jackson.databind.JavaType?): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
                    if (baseType?.rawClass == datetime::class.java) {
                        return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
                    }
                    return LaissezFaireSubTypeValidator.instance.validateBaseType(config, baseType)
                }
                
                override fun validateSubClassName(config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?, baseType: com.fasterxml.jackson.databind.JavaType?, subClassName: String?): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
                    if (subClassName?.contains("datetime") == true) {
                        return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
                    }
                    return LaissezFaireSubTypeValidator.instance.validateSubClassName(config, baseType, subClassName)
                }
                
                override fun validateSubType(config: com.fasterxml.jackson.databind.cfg.MapperConfig<*>?, baseType: com.fasterxml.jackson.databind.JavaType?, subType: com.fasterxml.jackson.databind.JavaType?): com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity {
                    if (subType?.rawClass == datetime::class.java) {
                        return com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator.Validity.DENIED
                    }
                    return LaissezFaireSubTypeValidator.instance.validateSubType(config, baseType, subType)
                }
            }
            
            // 启用多态类型处理以支持@JsonTypeInfo注解
            activateDefaultTyping(
                customValidator,
                ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }

    /**
     * 主要的RedisTemplate配置，使用带错误处理的Redis序列化器
     * 这个配置将覆盖生产环境的配置，确保测试环境的一致性
     * 使用高优先级确保在测试环境中完全覆盖生产配置
     */
    @Bean("redisTemplate")
    @Primary
    fun testRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        testRedisObjectMapper: ObjectMapper,
        redisSerializationErrorHandler: RedisSerializationErrorHandler
    ): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            
            // 使用带错误处理的序列化策略
            val jsonSerializer = ErrorHandlingRedisSerializer(testRedisObjectMapper, redisSerializationErrorHandler)
            
            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer
            
            afterPropertiesSet()
        }
    }

    /**
     * 专门用于ApiCallRecord的RedisTemplate
     * 使用带错误处理的Redis序列化器以保持与主RedisTemplate的一致性
     * 这样可以正确处理@class类型信息和各种序列化异常
     * 使用@Primary确保在测试环境中覆盖生产配置
     */
    @Bean("apiCallRecordRedisTemplate")
    @Primary
    fun testApiCallRecordRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        testRedisObjectMapper: ObjectMapper,
        redisSerializationErrorHandler: RedisSerializationErrorHandler
    ): RedisTemplate<String, ApiCallRecord?> {
        return RedisTemplate<String, ApiCallRecord?>().apply {
            this.connectionFactory = connectionFactory
            
            // 使用带错误处理的序列化器以保持与主RedisTemplate的一致性
            // 这样可以正确处理@class类型信息，避免序列化/反序列化不匹配的问题
            val jsonSerializer = ErrorHandlingRedisSerializer(testRedisObjectMapper, redisSerializationErrorHandler)
            
            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer
            
            afterPropertiesSet()
        }
    }

    /**
     * 额外的RedisTemplate bean，确保覆盖任何可能的默认配置
     */
    @Bean("stringRedisTemplate")
    fun testStringRedisTemplate(
        connectionFactory: RedisConnectionFactory,
        testRedisObjectMapper: ObjectMapper,
        redisSerializationErrorHandler: RedisSerializationErrorHandler
    ): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            
            val jsonSerializer = ErrorHandlingRedisSerializer(testRedisObjectMapper, redisSerializationErrorHandler)
            
            keySerializer = StringRedisSerializer()
            valueSerializer = jsonSerializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = jsonSerializer
            
            afterPropertiesSet()
        }
    }
}
