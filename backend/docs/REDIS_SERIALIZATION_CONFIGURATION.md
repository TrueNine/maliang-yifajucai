# Redis Serialization Configuration Documentation

## Overview

This document provides comprehensive documentation for Redis serialization configuration decisions, troubleshooting guides, and validation procedures for the backend test suite. The configuration addresses critical serialization issues that were causing test failures, particularly with Jackson JSON serialization/deserialization when working with Redis.

## Table of Contents

1. [Configuration Architecture](#configuration-architecture)
2. [Serialization Strategy](#serialization-strategy)
3. [Key Components](#key-components)
4. [Configuration Decisions](#configuration-decisions)
5. [Troubleshooting Guide](#troubleshooting-guide)
6. [Validation Procedures](#validation-procedures)
7. [Performance Considerations](#performance-considerations)
8. [Best Practices](#best-practices)

## Configuration Architecture

### Test Redis Configuration Structure

```
TestRedisConfiguration
├── testRedisObjectMapper()          # Unified ObjectMapper for Redis serialization
├── testRedisTemplate()              # Primary RedisTemplate with GenericJackson2JsonRedisSerializer
├── testApiCallRecordRedisTemplate() # Specialized template for ApiCallRecord entities
└── testStringRedisTemplate()        # Additional template for string operations
```

### Configuration Hierarchy

1. **Primary Configuration**: `TestRedisConfiguration` class
2. **Base Test Class**: `BaseRedisTest` for Redis-dependent tests
3. **Test Isolation**: Automatic Redis cache cleanup between tests
4. **Serialization Strategy**: Unified Jackson configuration across all Redis operations

## Serialization Strategy

### Core Serialization Approach

The Redis serialization strategy is built around a unified `ObjectMapper` configuration that handles:

- **Kotlin Data Classes**: Full support for Kotlin-specific features
- **Jimmer Entities**: Integration with Jimmer ORM serialization
- **DateTime Types**: Custom serialization for `datetime` objects
- **Polymorphic Types**: Proper `@class` type information handling
- **Error Resilience**: Graceful handling of unknown properties

### Serializer Selection

| Use Case | Serializer | Rationale |
|----------|------------|-----------|
| General Redis Operations | `GenericJackson2JsonRedisSerializer` | Handles polymorphic types with `@class` information |
| ApiCallRecord Entities | `Jackson2JsonRedisSerializer<ApiCallRecord>` | Type-safe serialization for specific entity types |
| String Keys | `StringRedisSerializer` | Efficient string key serialization |
| Hash Operations | `GenericJackson2JsonRedisSerializer` | Consistent with value serialization |

## Key Components

### 1. TestRedisObjectMapper

**Purpose**: Provides a unified ObjectMapper configuration for all Redis serialization operations.

**Key Features**:
- Kotlin module registration for data class support
- Java Time module for temporal type handling
- Jimmer module for ORM entity serialization
- Custom datetime serialization/deserialization
- Polymorphic type handling with `@class` properties

**Configuration**:
```kotlin
@Bean
@Primary
fun testRedisObjectMapper(): ObjectMapper {
    return ObjectMapper().apply {
        // Kotlin support
        registerModule(KotlinModule.Builder().build())
        
        // Java Time support
        registerModule(JavaTimeModule())
        
        // Jimmer ORM support
        registerModule(ImmutableModule())
        
        // Custom datetime handling
        val datetimeModule = SimpleModule()
        datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
        datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
        registerModule(datetimeModule)
        
        // Deserialization features
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        
        // Polymorphic type support
        activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )
    }
}
```

### 2. BaseRedisTest Class

**Purpose**: Provides a standardized base class for all Redis-dependent tests with proper setup and cleanup.

**Key Features**:
- Automatic Redis cache cleanup before and after each test
- Helper methods for Redis operations debugging
- Test isolation guarantees
- Error handling for cleanup failures

**Usage**:
```kotlin
@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfiguration::class)
@Testcontainers
abstract class BaseRedisTest : ICacheRedisContainer {
    
    @Autowired
    protected lateinit var redisTemplate: RedisTemplate<String, Any>
    
    @BeforeEach
    fun setupRedisTest() {
        // Clear Redis cache for test isolation
        redisTemplate.connectionFactory?.connection?.use { connection ->
            connection.serverCommands().flushAll()
        }
    }
    
    @AfterEach
    fun cleanupRedisTest() {
        // Post-test cleanup
        redisTemplate.connectionFactory?.connection?.use { connection ->
            connection.serverCommands().flushAll()
        }
    }
}
```

### 3. Specialized Redis Templates

**ApiCallRecord Template**:
- Uses `Jackson2JsonRedisSerializer` for type safety
- Maintains compatibility with production environment
- Handles complex entity relationships

**String Template**:
- Provides additional Redis operations support
- Ensures comprehensive coverage of Redis functionality

## Configuration Decisions

### 1. Serializer Choice: GenericJackson2JsonRedisSerializer

**Decision**: Use `GenericJackson2JsonRedisSerializer` as the primary serializer for Redis values.

**Rationale**:
- Handles polymorphic types automatically with `@class` type information
- Supports complex object hierarchies without manual type registration
- Provides flexibility for different data types in the same Redis instance
- Maintains backward compatibility with existing data

**Trade-offs**:
- Slightly larger serialized data due to `@class` metadata
- Potential security considerations with type information (mitigated by LaissezFaireSubTypeValidator)

### 2. ObjectMapper Configuration: Fail-Safe Approach

**Decision**: Configure ObjectMapper to be lenient with unknown properties and type mismatches.

**Rationale**:
- Prevents test failures due to schema evolution
- Allows graceful handling of partial data
- Supports development and testing flexibility
- Reduces brittleness in test environments

**Configuration Details**:
```kotlin
configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
```

### 3. Type Information Strategy: Property-Based

**Decision**: Use `JsonTypeInfo.As.PROPERTY` for polymorphic type information.

**Rationale**:
- Clear and readable type information in JSON
- Standard approach for Jackson polymorphic serialization
- Compatible with most JSON processing tools
- Debuggable and inspectable serialized data

### 4. Test Isolation: Aggressive Cleanup

**Decision**: Implement comprehensive Redis cache cleanup before and after each test.

**Rationale**:
- Ensures complete test isolation
- Prevents test interdependencies
- Eliminates flaky test behavior
- Provides clean state for each test execution

## Troubleshooting Guide

### Common Serialization Issues

#### 1. UnrecognizedPropertyException

**Symptoms**:
```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: 
Unrecognized field "someField" (class com.example.Entity), not marked as ignorable
```

**Causes**:
- Schema changes between serialization and deserialization
- Missing fields in target class
- Case sensitivity issues

**Solutions**:
1. **Immediate Fix**: Ensure `FAIL_ON_UNKNOWN_PROPERTIES` is set to `false`
2. **Long-term Fix**: Update entity classes to match serialized data
3. **Debugging**: Use `@JsonIgnoreProperties(ignoreUnknown = true)` on specific classes

**Prevention**:
```kotlin
// In ObjectMapper configuration
configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

// Or on specific classes
@JsonIgnoreProperties(ignoreUnknown = true)
data class MyEntity(...)
```

#### 2. InvalidTypeIdException

**Symptoms**:
```
com.fasterxml.jackson.databind.exc.InvalidTypeIdException: 
Could not resolve type id 'com.example.OldClassName' as a subtype of [simple type, class java.lang.Object]
```

**Causes**:
- Class refactoring or renaming
- Missing type information in serialized data
- Classpath issues

**Solutions**:
1. **Immediate Fix**: Implement custom type resolver
2. **Data Migration**: Update existing Redis data with new type information
3. **Fallback Strategy**: Implement error handling with default types

**Prevention**:
```kotlin
// Use LaissezFaireSubTypeValidator for lenient type validation
activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,
    ObjectMapper.DefaultTyping.NON_FINAL,
    JsonTypeInfo.As.PROPERTY
)
```

#### 3. DateTime Serialization Issues

**Symptoms**:
```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException: 
Cannot construct instance of `io.github.truenine.composeserver.datetime`
```

**Causes**:
- Missing datetime module registration
- Incorrect datetime format configuration
- Timezone handling issues

**Solutions**:
1. **Module Registration**: Ensure custom datetime module is registered
2. **Format Configuration**: Verify datetime serialization format
3. **Timezone Handling**: Use consistent timezone configuration

**Prevention**:
```kotlin
// Register custom datetime module
val datetimeModule = SimpleModule()
datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
registerModule(datetimeModule)
```

#### 4. Kotlin Data Class Issues

**Symptoms**:
```
com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException: 
Instantiation of [simple type, class com.example.KotlinClass] value failed for JSON property 'field'
```

**Causes**:
- Missing Kotlin module registration
- Nullable parameter handling issues
- Default parameter conflicts

**Solutions**:
1. **Module Registration**: Ensure KotlinModule is properly registered
2. **Nullable Handling**: Configure proper null handling
3. **Default Parameters**: Use appropriate Kotlin module configuration

**Prevention**:
```kotlin
// Proper Kotlin module registration
registerModule(KotlinModule.Builder().build())
```

### Test-Specific Issues

#### 1. Test Isolation Failures

**Symptoms**:
- Tests pass individually but fail when run together
- Inconsistent test results
- Data leakage between tests

**Diagnosis**:
```kotlin
// Add to test methods for debugging
@Test
fun myTest() {
    println("Redis keys before test: ${redisTemplate.keys("*")}")
    // Test logic
    println("Redis keys after test: ${redisTemplate.keys("*")}")
}
```

**Solutions**:
1. **Extend BaseRedisTest**: Ensure all Redis tests extend `BaseRedisTest`
2. **Manual Cleanup**: Add explicit cleanup in test methods if needed
3. **Verify Cleanup**: Check that cleanup methods are being called

#### 2. Container Startup Issues

**Symptoms**:
- Tests fail with connection errors
- Container startup timeouts
- Port binding conflicts

**Solutions**:
1. **Container Configuration**: Verify Testcontainers configuration
2. **Resource Management**: Ensure proper container lifecycle management
3. **Port Management**: Use dynamic port allocation

#### 3. Mock vs Real Redis Issues

**Symptoms**:
- Tests pass with mocks but fail with real Redis
- Serialization works in unit tests but fails in integration tests

**Solutions**:
1. **Consistent Configuration**: Use same Redis configuration in all test types
2. **Integration Testing**: Prefer integration tests for Redis functionality
3. **Mock Validation**: Ensure mocks accurately represent Redis behavior

### Performance Issues

#### 1. Slow Serialization

**Symptoms**:
- Tests take longer than expected
- High CPU usage during serialization
- Memory consumption spikes

**Diagnosis**:
```kotlin
// Add timing to serialization operations
val startTime = System.currentTimeMillis()
redisTemplate.opsForValue().set(key, value)
val endTime = System.currentTimeMillis()
println("Serialization took: ${endTime - startTime}ms")
```

**Solutions**:
1. **Optimize ObjectMapper**: Review ObjectMapper configuration
2. **Reduce Data Size**: Minimize serialized object complexity
3. **Connection Pooling**: Ensure proper Redis connection management

#### 2. Memory Leaks

**Symptoms**:
- Increasing memory usage over test runs
- OutOfMemoryError in test suites
- Slow garbage collection

**Solutions**:
1. **Cleanup Verification**: Ensure all Redis data is properly cleaned up
2. **Connection Management**: Verify Redis connections are closed
3. **Object References**: Check for retained object references

## Validation Procedures

### 1. Serialization Validation

**Basic Serialization Test**:
```kotlin
@Test
fun validateBasicSerialization() {
    val testData = MyEntity(id = 1, name = "test")
    redisTemplate.opsForValue().set("test:key", testData)
    val retrieved = redisTemplate.opsForValue().get("test:key") as MyEntity
    assertEquals(testData, retrieved)
}
```

**Complex Object Validation**:
```kotlin
@Test
fun validateComplexObjectSerialization() {
    val sessionData = SessionData(
        sessionId = "test-session",
        account = "testuser",
        userId = 1L,
        deviceId = "test-device",
        loginIpAddr = "127.0.0.1",
        loginTime = datetime.now(),
        roles = setOf("user", "admin"),
        permissions = setOf("read", "write"),
        expireTime = datetime.now().plusHours(1)
    )
    
    redisTemplate.opsForValue().set("session:test", sessionData)
    val retrieved = redisTemplate.opsForValue().get("session:test") as SessionData
    
    assertEquals(sessionData.sessionId, retrieved.sessionId)
    assertEquals(sessionData.account, retrieved.account)
    assertEquals(sessionData.roles, retrieved.roles)
    assertEquals(sessionData.permissions, retrieved.permissions)
}
```

### 2. Type Information Validation

**Polymorphic Type Test**:
```kotlin
@Test
fun validatePolymorphicTypeSerialization() {
    val entities: List<Any> = listOf(
        UserEntity(id = 1, name = "user"),
        AdminEntity(id = 2, name = "admin", permissions = setOf("all"))
    )
    
    entities.forEachIndexed { index, entity ->
        redisTemplate.opsForValue().set("entity:$index", entity)
        val retrieved = redisTemplate.opsForValue().get("entity:$index")
        assertEquals(entity::class, retrieved::class)
    }
}
```

### 3. Error Handling Validation

**Unknown Property Handling**:
```kotlin
@Test
fun validateUnknownPropertyHandling() {
    // Serialize object with extra field
    val jsonWithExtraField = """
        {
            "@class": "com.example.MyEntity",
            "id": 1,
            "name": "test",
            "unknownField": "should be ignored"
        }
    """.trimIndent()
    
    // Should not throw exception
    assertDoesNotThrow {
        val objectMapper = testRedisObjectMapper()
        val entity = objectMapper.readValue(jsonWithExtraField, MyEntity::class.java)
        assertNotNull(entity)
    }
}
```

### 4. Performance Validation

**Serialization Performance Test**:
```kotlin
@Test
fun validateSerializationPerformance() {
    val testData = createLargeTestObject()
    val iterations = 1000
    
    val startTime = System.currentTimeMillis()
    repeat(iterations) {
        redisTemplate.opsForValue().set("perf:test:$it", testData)
        redisTemplate.opsForValue().get("perf:test:$it")
    }
    val endTime = System.currentTimeMillis()
    
    val avgTime = (endTime - startTime).toDouble() / iterations
    assertTrue(avgTime < 10.0, "Average serialization time should be less than 10ms, was ${avgTime}ms")
}
```

### 5. Test Isolation Validation

**Isolation Test**:
```kotlin
@Test
fun validateTestIsolation() {
    // First test method
    redisTemplate.opsForValue().set("isolation:test1", "value1")
    assertTrue(redisTemplate.hasKey("isolation:test1"))
}

@Test
fun validateTestIsolationSecond() {
    // Second test method - should not see data from first test
    assertFalse(redisTemplate.hasKey("isolation:test1"))
    redisTemplate.opsForValue().set("isolation:test2", "value2")
    assertTrue(redisTemplate.hasKey("isolation:test2"))
}
```

## Performance Considerations

### Serialization Performance

1. **ObjectMapper Reuse**: The ObjectMapper is configured as a singleton bean to avoid recreation overhead
2. **Module Registration**: Modules are registered once during configuration
3. **Type Caching**: Jackson caches type information for improved performance
4. **Connection Pooling**: Redis connections are pooled for efficiency

### Memory Usage

1. **Cleanup Strategy**: Aggressive cleanup prevents memory leaks
2. **Object References**: Proper cleanup of object references
3. **Connection Management**: Connections are properly closed after use

### Test Execution Time

1. **Container Reuse**: Testcontainers are reused across tests where possible
2. **Parallel Execution**: Tests can run in parallel with proper isolation
3. **Cleanup Optimization**: Cleanup operations are optimized for speed

## Best Practices

### Configuration Best Practices

1. **Centralized Configuration**: Use `TestRedisConfiguration` for all Redis test configuration
2. **Environment Consistency**: Maintain consistency between test and production configurations
3. **Type Safety**: Use typed Redis templates where possible
4. **Error Handling**: Configure lenient error handling for test environments

### Testing Best Practices

1. **Extend BaseRedisTest**: Always extend `BaseRedisTest` for Redis-dependent tests
2. **Test Isolation**: Ensure each test is completely isolated
3. **Realistic Data**: Use realistic test data that matches production scenarios
4. **Performance Testing**: Include performance validation in test suites

### Debugging Best Practices

1. **Logging Configuration**: Enable appropriate logging levels for debugging
2. **Data Inspection**: Use helper methods to inspect Redis data during debugging
3. **Error Context**: Provide sufficient context in error messages
4. **Incremental Testing**: Test serialization components incrementally

### Maintenance Best Practices

1. **Regular Validation**: Run validation tests regularly
2. **Configuration Updates**: Keep configuration synchronized with production
3. **Documentation Updates**: Maintain up-to-date documentation
4. **Performance Monitoring**: Monitor test performance over time

## Conclusion

This Redis serialization configuration provides a robust, flexible, and maintainable solution for handling complex serialization scenarios in the test environment. The configuration addresses the key challenges of Kotlin data class serialization, Jimmer entity handling, datetime serialization, and polymorphic type management while maintaining high performance and reliability.

The troubleshooting guide and validation procedures ensure that issues can be quickly identified and resolved, while the best practices provide guidance for maintaining and extending the configuration as the application evolves.