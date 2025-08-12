# Redis Serialization Troubleshooting Guide

## Quick Reference

This guide provides step-by-step troubleshooting procedures for common Redis serialization issues encountered in the test environment.

## Table of Contents

1. [Quick Diagnostics](#quick-diagnostics)
2. [Common Error Patterns](#common-error-patterns)
3. [Step-by-Step Troubleshooting](#step-by-step-troubleshooting)
4. [Debugging Tools](#debugging-tools)
5. [Emergency Fixes](#emergency-fixes)
6. [Prevention Strategies](#prevention-strategies)

## Quick Diagnostics

### 1. Test Failure Checklist

When a Redis-related test fails, check these items first:

```bash
# 1. Check if Redis container is running
docker ps | grep redis

# 2. Check test configuration
grep -r "TestRedisConfiguration" backend/src/test/

# 3. Check for serialization errors in logs
grep -i "serialization\|jackson\|redis" backend/build/test-results/test/

# 4. Verify test isolation
grep -r "BaseRedisTest" backend/src/test/
```

### 2. Common Symptoms and Quick Fixes

| Symptom | Quick Fix | Full Solution |
|---------|-----------|---------------|
| `UnrecognizedPropertyException` | Add `@JsonIgnoreProperties(ignoreUnknown = true)` | Configure ObjectMapper properly |
| `InvalidTypeIdException` | Clear Redis cache manually | Implement type migration |
| Test isolation failures | Extend `BaseRedisTest` | Review test setup |
| Slow serialization | Check object complexity | Optimize ObjectMapper |
| Container startup issues | Restart Docker | Review Testcontainers config |

## Common Error Patterns

### 1. UnrecognizedPropertyException

**Full Error**:
```
com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: 
Unrecognized field "newField" (class com.example.Entity), not marked as ignorable
 at [Source: (String)"{"@class":"com.example.Entity","id":1,"newField":"value"}"; line: 1, column: 45]
```

**Root Causes**:
1. Schema evolution - new fields added to classes
2. Test data contains fields not in current class definition
3. ObjectMapper not configured to ignore unknown properties

**Diagnostic Steps**:
```kotlin
// 1. Check ObjectMapper configuration
@Test
fun checkObjectMapperConfig() {
    val mapper = testRedisObjectMapper()
    val config = mapper.deserializationConfig
    println("FAIL_ON_UNKNOWN_PROPERTIES: ${config.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)}")
}

// 2. Inspect Redis data
@Test
fun inspectRedisData() {
    val rawData = redisTemplate.opsForValue().get("problematic:key")
    println("Raw Redis data: $rawData")
}

// 3. Test serialization manually
@Test
fun testManualSerialization() {
    val mapper = testRedisObjectMapper()
    val json = """{"@class":"com.example.Entity","id":1,"unknownField":"value"}"""
    try {
        val entity = mapper.readValue(json, Entity::class.java)
        println("Deserialization successful: $entity")
    } catch (e: Exception) {
        println("Deserialization failed: ${e.message}")
    }
}
```

**Solutions**:
```kotlin
// Solution 1: Configure ObjectMapper (Recommended)
@Bean
fun testRedisObjectMapper(): ObjectMapper {
    return ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // ... other configuration
    }
}

// Solution 2: Class-level annotation
@JsonIgnoreProperties(ignoreUnknown = true)
data class MyEntity(
    val id: Long,
    val name: String
)

// Solution 3: Field-level handling
data class MyEntity(
    val id: Long,
    val name: String,
    @JsonIgnore
    val ignoredField: String? = null
)
```

### 2. InvalidTypeIdException

**Full Error**:
```
com.fasterxml.jackson.databind.exc.InvalidTypeIdException: 
Could not resolve type id 'com.example.OldClassName' as a subtype of [simple type, class java.lang.Object]
```

**Root Causes**:
1. Class renamed or moved to different package
2. Existing Redis data contains old type information
3. Classpath issues preventing class loading

**Diagnostic Steps**:
```kotlin
// 1. Check existing Redis data
@Test
fun checkExistingRedisData() {
    val keys = redisTemplate.keys("*")
    keys.forEach { key ->
        val rawValue = redisTemplate.opsForValue().get(key)
        println("Key: $key, Value: $rawValue")
        if (rawValue.toString().contains("@class")) {
            println("Contains type information: $rawValue")
        }
    }
}

// 2. Verify class availability
@Test
fun verifyClassAvailability() {
    try {
        val clazz = Class.forName("com.example.OldClassName")
        println("Class found: ${clazz.name}")
    } catch (e: ClassNotFoundException) {
        println("Class not found: ${e.message}")
    }
}

// 3. Test type resolution
@Test
fun testTypeResolution() {
    val mapper = testRedisObjectMapper()
    val typeFactory = mapper.typeFactory
    try {
        val type = typeFactory.constructFromCanonical("com.example.OldClassName")
        println("Type resolved: $type")
    } catch (e: Exception) {
        println("Type resolution failed: ${e.message}")
    }
}
```

**Solutions**:
```kotlin
// Solution 1: Clear problematic Redis data
@BeforeEach
fun clearProblematicData() {
    redisTemplate.connectionFactory?.connection?.use { connection ->
        connection.serverCommands().flushAll()
    }
}

// Solution 2: Implement custom type resolver
@Bean
fun customTypeResolver(): ObjectMapper {
    return ObjectMapper().apply {
        val typeResolver = object : TypeIdResolver {
            override fun idFromValue(value: Any?): String? {
                return value?.javaClass?.name
            }
            
            override fun typeFromId(context: DatabindContext?, id: String?): JavaType? {
                return when (id) {
                    "com.example.OldClassName" -> context?.constructType(NewClassName::class.java)
                    else -> context?.constructType(Object::class.java)
                }
            }
            // ... implement other methods
        }
        // Configure with custom resolver
    }
}

// Solution 3: Use LaissezFaireSubTypeValidator
activateDefaultTyping(
    LaissezFaireSubTypeValidator.instance,
    ObjectMapper.DefaultTyping.NON_FINAL,
    JsonTypeInfo.As.PROPERTY
)
```

### 3. DateTime Serialization Issues

**Full Error**:
```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException: 
Cannot construct instance of `io.github.truenine.composeserver.datetime`
```

**Root Causes**:
1. Missing datetime module registration
2. Incorrect datetime format
3. Timezone handling issues

**Diagnostic Steps**:
```kotlin
// 1. Test datetime serialization
@Test
fun testDatetimeSerialization() {
    val mapper = testRedisObjectMapper()
    val now = datetime.now()
    
    try {
        val json = mapper.writeValueAsString(now)
        println("Serialized datetime: $json")
        
        val deserialized = mapper.readValue(json, datetime::class.java)
        println("Deserialized datetime: $deserialized")
        
        assertEquals(now, deserialized)
    } catch (e: Exception) {
        println("DateTime serialization failed: ${e.message}")
        e.printStackTrace()
    }
}

// 2. Check module registration
@Test
fun checkModuleRegistration() {
    val mapper = testRedisObjectMapper()
    val modules = mapper.registeredModuleIds
    println("Registered modules: $modules")
    
    val hasDatetimeModule = modules.any { it.contains("datetime") }
    println("Has datetime module: $hasDatetimeModule")
}
```

**Solutions**:
```kotlin
// Solution 1: Register datetime module properly
@Bean
fun testRedisObjectMapper(): ObjectMapper {
    return ObjectMapper().apply {
        // Register Java Time module
        registerModule(JavaTimeModule())
        
        // Register custom datetime module
        val datetimeModule = SimpleModule()
        datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
        datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
        registerModule(datetimeModule)
    }
}

// Solution 2: Custom datetime serializer
class DatetimeSerializer : JsonSerializer<datetime>() {
    override fun serialize(value: datetime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value != null && gen != null) {
            gen.writeString(value.toString())
        }
    }
}

class DatetimeDeserializer : JsonDeserializer<datetime>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): datetime? {
        return p?.text?.let { datetime.parse(it) }
    }
}
```

### 4. Test Isolation Failures

**Symptoms**:
- Tests pass individually but fail when run together
- Inconsistent test results
- Data from previous tests affecting current test

**Diagnostic Steps**:
```kotlin
// 1. Check test inheritance
@Test
fun checkTestInheritance() {
    val testClass = this::class.java
    val superClass = testClass.superclass
    println("Test class: ${testClass.name}")
    println("Super class: ${superClass?.name}")
    println("Extends BaseRedisTest: ${BaseRedisTest::class.java.isAssignableFrom(testClass)}")
}

// 2. Monitor Redis state
@BeforeEach
fun monitorRedisState() {
    val keysBefore = redisTemplate.keys("*")
    println("Redis keys before test: $keysBefore")
}

@AfterEach
fun monitorRedisStateAfter() {
    val keysAfter = redisTemplate.keys("*")
    println("Redis keys after test: $keysAfter")
}

// 3. Test cleanup effectiveness
@Test
fun testCleanupEffectiveness() {
    // Add test data
    redisTemplate.opsForValue().set("test:cleanup", "value")
    assertTrue(redisTemplate.hasKey("test:cleanup"))
    
    // Manually trigger cleanup
    redisTemplate.connectionFactory?.connection?.use { connection ->
        connection.serverCommands().flushAll()
    }
    
    // Verify cleanup
    assertFalse(redisTemplate.hasKey("test:cleanup"))
}
```

**Solutions**:
```kotlin
// Solution 1: Extend BaseRedisTest
@SpringBootTest
@ActiveProfiles("test")
@Import(TestRedisConfiguration::class)
@Testcontainers
class MyRedisTest : BaseRedisTest() {
    // Test methods
}

// Solution 2: Manual cleanup in test methods
@Test
fun myTestWithManualCleanup() {
    try {
        // Test logic
    } finally {
        // Ensure cleanup
        redisTemplate.connectionFactory?.connection?.use { connection ->
            connection.serverCommands().flushAll()
        }
    }
}

// Solution 3: Enhanced BaseRedisTest
abstract class BaseRedisTest : ICacheRedisContainer {
    
    @Autowired
    protected lateinit var redisTemplate: RedisTemplate<String, Any>
    
    @BeforeEach
    fun setupRedisTest() {
        cleanupRedis()
    }
    
    @AfterEach
    fun cleanupRedisTest() {
        cleanupRedis()
    }
    
    private fun cleanupRedis() {
        try {
            redisTemplate.connectionFactory?.connection?.use { connection ->
                connection.serverCommands().flushAll()
            }
        } catch (e: Exception) {
            println("Warning: Redis cleanup failed: ${e.message}")
            // Try alternative cleanup
            try {
                val keys = redisTemplate.keys("*")
                if (keys.isNotEmpty()) {
                    redisTemplate.delete(keys)
                }
            } catch (e2: Exception) {
                println("Alternative cleanup also failed: ${e2.message}")
            }
        }
    }
}
```

## Step-by-Step Troubleshooting

### When a Redis Test Fails

#### Step 1: Identify the Error Type
```bash
# Check test output for error patterns
grep -E "(UnrecognizedProperty|InvalidTypeId|SerializationException)" test-output.log

# Check for Redis connection issues
grep -E "(connection|redis|container)" test-output.log

# Check for test isolation issues
grep -E "(isolation|cleanup|BaseRedisTest)" test-output.log
```

#### Step 2: Verify Configuration
```kotlin
// Add this test to verify configuration
@Test
fun verifyRedisConfiguration() {
    // Check ObjectMapper
    val mapper = testRedisObjectMapper()
    assertNotNull(mapper)
    
    // Check Redis template
    assertNotNull(redisTemplate)
    assertNotNull(redisTemplate.valueSerializer)
    
    // Check serializer type
    val serializer = redisTemplate.valueSerializer
    println("Value serializer: ${serializer?.javaClass?.name}")
    
    // Test basic operation
    redisTemplate.opsForValue().set("config:test", "value")
    val retrieved = redisTemplate.opsForValue().get("config:test")
    assertEquals("value", retrieved)
}
```

#### Step 3: Test Serialization Manually
```kotlin
@Test
fun testSerializationManually() {
    val mapper = testRedisObjectMapper()
    
    // Test with simple object
    val simpleObject = mapOf("key" to "value")
    val simpleJson = mapper.writeValueAsString(simpleObject)
    val simpleDeserialized = mapper.readValue(simpleJson, Map::class.java)
    assertEquals(simpleObject, simpleDeserialized)
    
    // Test with complex object
    val complexObject = SessionData(
        sessionId = "test",
        account = "user",
        userId = 1L,
        deviceId = "device",
        loginIpAddr = "127.0.0.1",
        loginTime = datetime.now(),
        roles = setOf("user"),
        permissions = setOf("read"),
        expireTime = datetime.now().plusHours(1)
    )
    
    val complexJson = mapper.writeValueAsString(complexObject)
    println("Complex JSON: $complexJson")
    
    val complexDeserialized = mapper.readValue(complexJson, SessionData::class.java)
    assertEquals(complexObject.sessionId, complexDeserialized.sessionId)
}
```

#### Step 4: Check Test Environment
```kotlin
@Test
fun checkTestEnvironment() {
    // Check Spring profile
    val environment = applicationContext.environment
    val activeProfiles = environment.activeProfiles
    println("Active profiles: ${activeProfiles.joinToString()}")
    
    // Check Redis connection
    val connection = redisTemplate.connectionFactory?.connection
    assertNotNull(connection)
    println("Redis connection: ${connection?.javaClass?.name}")
    
    // Check container status
    if (this is ICacheRedisContainer) {
        println("Redis container running: ${redisContainer.isRunning}")
        println("Redis container port: ${redisContainer.getMappedPort(6379)}")
    }
}
```

#### Step 5: Isolate the Problem
```kotlin
// Create minimal reproduction test
@Test
fun minimalReproduction() {
    // Use the exact same data that's failing
    val problematicData = createProblematicData()
    
    // Test each step individually
    
    // Step 1: Serialization
    val mapper = testRedisObjectMapper()
    val json = mapper.writeValueAsString(problematicData)
    println("Serialization successful: $json")
    
    // Step 2: Deserialization
    val deserialized = mapper.readValue(json, problematicData::class.java)
    println("Deserialization successful: $deserialized")
    
    // Step 3: Redis storage
    redisTemplate.opsForValue().set("test:problematic", problematicData)
    println("Redis storage successful")
    
    // Step 4: Redis retrieval
    val retrieved = redisTemplate.opsForValue().get("test:problematic")
    println("Redis retrieval successful: $retrieved")
    
    // Step 5: Type verification
    assertEquals(problematicData::class, retrieved::class)
    println("Type verification successful")
}
```

## Debugging Tools

### 1. Redis Data Inspector
```kotlin
@Component
class RedisDataInspector {
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>
    
    fun inspectAllKeys(): Map<String, Any?> {
        val keys = redisTemplate.keys("*")
        return keys.associateWith { key ->
            try {
                redisTemplate.opsForValue().get(key)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }
    
    fun inspectKey(key: String): RedisKeyInfo {
        return RedisKeyInfo(
            key = key,
            exists = redisTemplate.hasKey(key),
            type = redisTemplate.type(key)?.name,
            ttl = redisTemplate.getExpire(key),
            value = try {
                redisTemplate.opsForValue().get(key)
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        )
    }
}

data class RedisKeyInfo(
    val key: String,
    val exists: Boolean,
    val type: String?,
    val ttl: Long,
    val value: Any?
)
```

### 2. Serialization Debugger
```kotlin
@Component
class SerializationDebugger {
    
    fun debugSerialization(obj: Any, mapper: ObjectMapper): SerializationDebugInfo {
        return try {
            val json = mapper.writeValueAsString(obj)
            val deserialized = mapper.readValue(json, obj::class.java)
            
            SerializationDebugInfo(
                success = true,
                originalObject = obj,
                serializedJson = json,
                deserializedObject = deserialized,
                typesMatch = obj::class == deserialized::class,
                error = null
            )
        } catch (e: Exception) {
            SerializationDebugInfo(
                success = false,
                originalObject = obj,
                serializedJson = null,
                deserializedObject = null,
                typesMatch = false,
                error = e.message
            )
        }
    }
}

data class SerializationDebugInfo(
    val success: Boolean,
    val originalObject: Any,
    val serializedJson: String?,
    val deserializedObject: Any?,
    val typesMatch: Boolean,
    val error: String?
)
```

### 3. Test Isolation Monitor
```kotlin
@Component
class TestIsolationMonitor {
    
    private val testStates = mutableMapOf<String, RedisState>()
    
    fun recordTestStart(testName: String, redisTemplate: RedisTemplate<String, Any>) {
        val keys = redisTemplate.keys("*")
        testStates[testName] = RedisState(
            testName = testName,
            startTime = System.currentTimeMillis(),
            keysAtStart = keys.toSet(),
            keysAtEnd = emptySet()
        )
    }
    
    fun recordTestEnd(testName: String, redisTemplate: RedisTemplate<String, Any>) {
        val keys = redisTemplate.keys("*")
        testStates[testName]?.let { state ->
            testStates[testName] = state.copy(
                keysAtEnd = keys.toSet(),
                endTime = System.currentTimeMillis()
            )
        }
    }
    
    fun getIsolationReport(): List<IsolationIssue> {
        return testStates.values.mapNotNull { state ->
            if (state.keysAtEnd.isNotEmpty()) {
                IsolationIssue(
                    testName = state.testName,
                    issue = "Test left ${state.keysAtEnd.size} keys in Redis",
                    leftoverKeys = state.keysAtEnd
                )
            } else null
        }
    }
}

data class RedisState(
    val testName: String,
    val startTime: Long,
    val keysAtStart: Set<String>,
    val keysAtEnd: Set<String>,
    val endTime: Long? = null
)

data class IsolationIssue(
    val testName: String,
    val issue: String,
    val leftoverKeys: Set<String>
)
```

## Emergency Fixes

### 1. Complete Redis Reset
```bash
# Stop all containers
docker stop $(docker ps -q --filter ancestor=redis)

# Remove Redis containers
docker rm $(docker ps -aq --filter ancestor=redis)

# Clean up volumes
docker volume prune -f

# Restart tests
./gradlew clean test
```

### 2. Force Clean Test Environment
```kotlin
// Add to test class
@BeforeAll
@JvmStatic
fun forceCleanEnvironment() {
    // Clear all Redis data
    try {
        val redisTemplate = applicationContext.getBean(RedisTemplate::class.java) as RedisTemplate<String, Any>
        redisTemplate.connectionFactory?.connection?.use { connection ->
            connection.serverCommands().flushAll()
        }
    } catch (e: Exception) {
        println("Could not clean Redis: ${e.message}")
    }
    
    // Clear any cached ObjectMappers
    System.gc()
}
```

### 3. Bypass Problematic Serialization
```kotlin
// Temporary workaround for specific objects
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE) // Disable type info temporarily
data class ProblematicEntity(
    val id: Long,
    val name: String
    // Remove problematic fields temporarily
)
```

## Prevention Strategies

### 1. Proactive Configuration Validation
```kotlin
@TestConfiguration
class RedisConfigurationValidator {
    
    @EventListener
    fun validateConfiguration(event: ContextRefreshedEvent) {
        val context = event.applicationContext
        
        // Validate ObjectMapper
        val mapper = context.getBean("testRedisObjectMapper", ObjectMapper::class.java)
        validateObjectMapper(mapper)
        
        // Validate RedisTemplate
        val redisTemplate = context.getBean("redisTemplate", RedisTemplate::class.java)
        validateRedisTemplate(redisTemplate)
    }
    
    private fun validateObjectMapper(mapper: ObjectMapper) {
        val config = mapper.deserializationConfig
        
        // Check critical settings
        assert(!config.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            "ObjectMapper should not fail on unknown properties"
        }
        
        // Check module registration
        val moduleIds = mapper.registeredModuleIds
        assert(moduleIds.any { it.contains("kotlin") }) {
            "Kotlin module should be registered"
        }
    }
    
    private fun validateRedisTemplate(redisTemplate: RedisTemplate<String, Any>) {
        assert(redisTemplate.valueSerializer != null) {
            "Redis value serializer should be configured"
        }
        
        assert(redisTemplate.keySerializer != null) {
            "Redis key serializer should be configured"
        }
    }
}
```

### 2. Automated Serialization Testing
```kotlin
@Component
class SerializationTestSuite {
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    fun runSerializationTests(): List<SerializationTestResult> {
        val testObjects = createTestObjects()
        
        return testObjects.map { obj ->
            try {
                val json = objectMapper.writeValueAsString(obj)
                val deserialized = objectMapper.readValue(json, obj::class.java)
                
                SerializationTestResult(
                    objectType = obj::class.simpleName ?: "Unknown",
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                SerializationTestResult(
                    objectType = obj::class.simpleName ?: "Unknown",
                    success = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun createTestObjects(): List<Any> {
        return listOf(
            // Add all your entity types here
            SessionData(/* ... */),
            ApiCallRecord(/* ... */),
            // etc.
        )
    }
}

data class SerializationTestResult(
    val objectType: String,
    val success: Boolean,
    val error: String?
)
```

### 3. Continuous Monitoring
```kotlin
@Component
class RedisHealthMonitor {
    
    @Scheduled(fixedRate = 60000) // Every minute during tests
    fun monitorRedisHealth() {
        try {
            // Check Redis connectivity
            val connection = redisTemplate.connectionFactory?.connection
            connection?.ping()
            
            // Check serialization health
            val testObject = mapOf("health" to "check")
            redisTemplate.opsForValue().set("health:check", testObject, Duration.ofSeconds(10))
            val retrieved = redisTemplate.opsForValue().get("health:check")
            
            if (retrieved != testObject) {
                println("WARNING: Redis serialization health check failed")
            }
            
        } catch (e: Exception) {
            println("ERROR: Redis health check failed: ${e.message}")
        }
    }
}
```

This troubleshooting guide provides comprehensive coverage of Redis serialization issues and their solutions. Use it as a reference when encountering problems, and follow the step-by-step procedures to systematically identify and resolve issues.
