# Design Document

## Overview

This design addresses the Redis serialization issues causing test failures in the backend test suite. The core problem is that Jackson's `GenericJackson2JsonRedisSerializer` and `Jackson2JsonRedisSerializer` are having conflicts with `@class` type information handling, particularly when dealing with Kotlin data classes, builders, and polymorphic types.

The solution involves standardizing Redis serialization configuration, fixing test setup issues, and ensuring proper isolation between tests.

## Architecture

### Current Issues Analysis

1. **Redis Serialization Conflicts**: Multiple serializers are configured with different `@class` handling strategies
2. **Test Isolation Problems**: Tests are not properly cleaning up Redis state between runs
3. **Configuration Inconsistencies**: Test and production Redis configurations differ
4. **Type Information Handling**: Jackson is struggling with Kotlin builders and data classes

### Proposed Solution Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Configuration Layer                  │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Redis Config    │  │ Jackson Config  │  │ Test Isolation  │ │
│  │ Standardization │  │ Standardization │  │ Management      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                    Serialization Layer                      │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Unified Redis   │  │ Type-Safe       │  │ Kotlin Data     │ │
│  │ Serializer      │  │ Serialization   │  │ Class Support   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                      Application Layer                      │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. Redis Configuration Component

**Purpose**: Standardize Redis serialization across test and production environments

**Key Classes**:
- `TestRedisConfiguration`: Test-specific Redis configuration
- `RedisSerializationConfig`: Unified serialization settings

**Configuration Strategy**:
```kotlin
@TestConfiguration
class TestRedisConfiguration {
    @Bean
    @Primary
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            // Use consistent serialization strategy
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        }
    }
}
```

### 2. Jackson Configuration Component

**Purpose**: Configure Jackson to handle Kotlin types and `@class` information correctly

**Key Features**:
- Proper `@class` type information handling
- Kotlin data class support
- Builder pattern support
- DateTime serialization

**Configuration Strategy**:
```kotlin
@Bean
fun testObjectMapper(): ObjectMapper {
    return ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
        activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )
    }
}
```

### 3. Test Isolation Component

**Purpose**: Ensure proper test isolation and cleanup

**Key Features**:
- Redis cache cleanup between tests
- Database state management
- Mock object reset
- Test container management

**Implementation Strategy**:
```kotlin
@TestMethodOrder(OrderAnnotation::class)
abstract class BaseRedisTest {
    @Autowired
    protected lateinit var redisTemplate: RedisTemplate<String, Any>
    
    @BeforeEach
    fun setupTest() {
        // Clear Redis cache
        redisTemplate.connectionFactory?.connection?.flushAll()
    }
    
    @AfterEach
    fun cleanupTest() {
        // Additional cleanup if needed
    }
}
```

## Data Models

### Serialization Data Models

The following data models need special serialization handling:

1. **ApiCallRecordDraft**: Jimmer entity builder with complex nested properties
2. **SessionData**: Contains datetime fields and complex object references
3. **UserPrincipal**: Contains role and permission collections
4. **Attachment entities**: Complex entity relationships

### Serialization Strategies

```kotlin
// For ApiCallRecordDraft
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApiCallRecordDraft(...)

// For SessionData
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
data class SessionData(
    val expireTime: LocalDateTime,
    ...
)
```

## Error Handling

### Serialization Error Handling

1. **UnrecognizedPropertyException**: Handle unknown properties gracefully
2. **InvalidTypeIdException**: Provide fallback type resolution
3. **SerializationException**: Implement retry mechanisms for transient failures

### Test Error Handling

1. **Test Isolation Failures**: Implement robust cleanup mechanisms
2. **Container Startup Failures**: Implement retry and fallback strategies
3. **Database Connection Issues**: Implement connection pooling and retry logic

### Error Recovery Strategies

```kotlin
@Component
class RedisSerializationErrorHandler {
    fun handleSerializationError(ex: SerializationException): Any? {
        when (ex.cause) {
            is UnrecognizedPropertyException -> {
                // Log and return null or default value
                logger.warn("Unknown property in Redis deserialization", ex)
                return null
            }
            is InvalidTypeIdException -> {
                // Attempt fallback deserialization
                return attemptFallbackDeserialization(ex)
            }
            else -> throw ex
        }
    }
}
```

## Testing Strategy

### Unit Testing Strategy

1. **Serialization Unit Tests**: Test each serialization scenario independently
2. **Configuration Unit Tests**: Verify Redis and Jackson configurations
3. **Isolation Unit Tests**: Verify test cleanup mechanisms

### Integration Testing Strategy

1. **Redis Integration Tests**: Test full Redis operations with real containers
2. **Service Integration Tests**: Test service layer with Redis dependencies
3. **End-to-End Tests**: Test complete workflows including serialization

### Test Data Management

```kotlin
@TestConfiguration
class TestDataConfiguration {
    @Bean
    fun testDataFactory(): TestDataFactory {
        return TestDataFactory().apply {
            // Configure test data creation strategies
            registerEntityFactory(ApiCallRecordDraft::class) { createTestApiCallRecord() }
            registerEntityFactory(SessionData::class) { createTestSessionData() }
        }
    }
}
```

### Performance Testing

1. **Serialization Performance**: Measure serialization/deserialization times
2. **Memory Usage**: Monitor memory consumption during tests
3. **Container Startup Time**: Optimize test container reuse

## Implementation Phases

### Phase 1: Configuration Standardization
- Standardize Redis configuration across test environments
- Configure Jackson for proper Kotlin support
- Implement base test classes for Redis tests

### Phase 2: Serialization Fixes
- Fix ApiCallRecordDraft serialization issues
- Fix SessionData datetime serialization
- Implement error handling for serialization failures

### Phase 3: Test Isolation
- Implement proper test cleanup mechanisms
- Fix test ordering and dependencies
- Optimize test container usage

### Phase 4: Validation and Optimization
- Run full test suite validation
- Performance optimization
- Documentation updates
