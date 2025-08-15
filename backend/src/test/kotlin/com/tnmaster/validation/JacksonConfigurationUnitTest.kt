package com.tnmaster.validation

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
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
import io.github.truenine.composeserver.datetime
import org.babyfish.jimmer.jackson.ImmutableModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Define polymorphic types outside the class for testing
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "@class"
)
@JsonSubTypes(
  JsonSubTypes.Type(value = UserEntity::class, name = "UserEntity"),
  JsonSubTypes.Type(value = AdminEntity::class, name = "AdminEntity")
)
interface BaseEntity {
  val id: Long
  val type: String
}

data class UserEntity(
  override val id: Long,
  override val type: String = "user",
  val username: String,
  val email: String,
) : BaseEntity

data class AdminEntity(
  override val id: Long,
  override val type: String = "admin",
  val username: String,
  val permissions: Set<String>,
) : BaseEntity

/**
 * Unit test for validating enhanced Jackson configuration for Kotlin data classes
 * This test runs without Spring context to isolate Jackson configuration issues
 */
@DisplayName("Jackson Kotlin Configuration Unit Test")
class JacksonConfigurationUnitTest {

  private lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setupObjectMapper() {
    objectMapper = createEnhancedObjectMapper()
  }

  private fun createEnhancedObjectMapper(): ObjectMapper {
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

      // Enhanced serialization features for consistent output
      configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
      configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false)

      // Enhanced polymorphic type handling with @class information
      // This is the key configuration for resolving @class property issues
      // Use NON_FINAL to include abstract classes and interfaces, with explicit @JsonTypeInfo for concrete classes
      activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY
      )
    }
  }

  @Test
  @DisplayName("1. Validate Enhanced Kotlin Module Configuration")
  fun validateKotlinModuleConfiguration() {
    val moduleIds = objectMapper.registeredModuleIds

    // Verify Kotlin module is registered
    assertTrue(
      moduleIds.any { it.toString().contains("kotlin", ignoreCase = true) },
      "Kotlin module should be registered"
    )

    // Verify Java Time module is registered
    assertTrue(
      moduleIds.any { it.toString().contains("JavaTime", ignoreCase = true) || it.toString().contains("jsr310", ignoreCase = true) },
      "Java Time module should be registered"
    )

    // Verify Jimmer module is registered
    assertTrue(
      moduleIds.any { it.toString().contains("ImmutableModule", ignoreCase = true) },
      "Jimmer module should be registered"
    )

    println("✅ Enhanced Kotlin module configuration validated")
    println("Registered modules: ${moduleIds.joinToString()}")
  }

  @Test
  @DisplayName("2. Validate Kotlin Data Class Serialization")
  fun validateKotlinDataClassSerialization() {
    // Test simple Kotlin data class
    data class SimpleKotlinClass(
      val id: Long,
      val name: String,
      val isActive: Boolean = true,
      val tags: List<String> = emptyList(),
    )

    val original = SimpleKotlinClass(
      id = 1L,
      name = "Test Entity",
      isActive = true,
      tags = listOf("tag1", "tag2")
    )

    // Test serialization and deserialization
    val json = objectMapper.writeValueAsString(original)
    assertNotNull(json, "JSON serialization should not be null")
    // Note: Data classes are final, so they won't have @class information with NON_FINAL setting
    // This is actually correct behavior for most use cases

    val deserialized = objectMapper.readValue(json, SimpleKotlinClass::class.java)
    assertEquals(original, deserialized, "Deserialized object should match original")

    println("✅ Kotlin data class serialization validated")
    println("Serialized JSON: $json")
  }

  @Test
  @DisplayName("3. Validate Kotlin Data Class with Nullable Properties")
  fun validateKotlinDataClassWithNullables() {
    data class NullableKotlinClass(
      val id: Long,
      val name: String?,
      val description: String? = null,
      val metadata: Map<String, Any?>? = null,
    )

    val original = NullableKotlinClass(
      id = 1L,
      name = null,
      description = "Test description",
      metadata = mapOf("key1" to "value1", "key2" to null)
    )

    val json = objectMapper.writeValueAsString(original)
    val deserialized = objectMapper.readValue(json, NullableKotlinClass::class.java)

    assertEquals(original.id, deserialized.id)
    assertEquals(original.name, deserialized.name)
    assertEquals(original.description, deserialized.description)
    assertEquals(original.metadata, deserialized.metadata)

    println("✅ Kotlin nullable properties serialization validated")
  }

  @Test
  @DisplayName("4. Validate Kotlin Data Class with Collections")
  fun validateKotlinDataClassWithCollections() {
    data class CollectionKotlinClass(
      val id: Long,
      val stringList: List<String>,
      val stringSet: Set<String>,
      val stringMap: Map<String, String>,
      val nestedList: List<Map<String, Any>>,
    )

    val original = CollectionKotlinClass(
      id = 1L,
      stringList = listOf("item1", "item2", "item3"),
      stringSet = setOf("set1", "set2", "set3"),
      stringMap = mapOf("key1" to "value1", "key2" to "value2"),
      nestedList = listOf(
        mapOf("nested1" to "value1", "nested2" to 123),
        mapOf("nested3" to "value3", "nested4" to true)
      )
    )

    val json = objectMapper.writeValueAsString(original)
    val deserialized = objectMapper.readValue(json, CollectionKotlinClass::class.java)

    assertEquals(original, deserialized, "Collections should be properly serialized and deserialized")

    println("✅ Kotlin collections serialization validated")
  }

  @Test
  @DisplayName("5. Validate Polymorphic Type Handling with @class")
  fun validatePolymorphicTypeHandling() {
    val entities: List<BaseEntity> = listOf(
      UserEntity(1L, "user", "john_doe", "john@example.com"),
      AdminEntity(2L, "admin", "admin_user", setOf("read", "write", "delete"))
    )

    // Test each entity type
    entities.forEach { entity ->
      val json = objectMapper.writeValueAsString(entity)
      println("Serialized entity ${entity::class.simpleName}: $json")
      assertTrue(json.contains("@class"), "JSON should contain @class type information for ${entity::class.simpleName}. Actual JSON: $json")

      val deserialized = objectMapper.readValue(json, BaseEntity::class.java)
      assertEquals(entity::class, deserialized::class, "Deserialized type should match original")
      assertEquals(entity.id, deserialized.id, "Entity properties should match")
    }

    println("✅ Polymorphic type handling validated")
  }

  @Test
  @DisplayName("6. Validate Unknown Property Handling")
  fun validateUnknownPropertyHandling() {
    data class TestEntity(val id: Long, val name: String)

    // JSON with unknown properties
    val jsonWithUnknownProps = """
            {
                "@class": "${TestEntity::class.java.name}",
                "id": 1,
                "name": "test",
                "unknownField": "should be ignored",
                "anotherUnknownField": 123
            }
        """.trimIndent()

    // Should not throw exception when deserializing
    val result = objectMapper.readValue(jsonWithUnknownProps, TestEntity::class.java)
    assertNotNull(result, "Result should not be null")
    assertEquals(1L, result.id, "Known field should be preserved")
    assertEquals("test", result.name, "Known field should be preserved")

    println("✅ Unknown property handling validated")
  }

  @Test
  @DisplayName("7. Validate DateTime Serialization with Kotlin Data Classes")
  fun validateDateTimeSerializationWithKotlin() {
    data class DateTimeEntity(
      val id: Long,
      val createdAt: datetime,
      val updatedAt: datetime?,
      val scheduledFor: datetime = datetime.now().plusDays(1),
    )

    val now = datetime.now()
    val entity = DateTimeEntity(
      id = 1L,
      createdAt = now,
      updatedAt = now.plusHours(1),
      scheduledFor = now.plusDays(2)
    )

    val json = objectMapper.writeValueAsString(entity)
    val deserialized = objectMapper.readValue(json, DateTimeEntity::class.java)

    assertEquals(entity.id, deserialized.id)
    assertEquals(entity.createdAt, deserialized.createdAt)
    assertEquals(entity.updatedAt, deserialized.updatedAt)
    assertEquals(entity.scheduledFor, deserialized.scheduledFor)

    println("✅ DateTime serialization with Kotlin data classes validated")
  }

  @Test
  @DisplayName("8. Validate Configuration Consistency")
  fun validateConfigurationConsistency() {
    val config = objectMapper.deserializationConfig

    // Verify critical deserialization features
    assertTrue(
      !config.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
      "Should not fail on unknown properties"
    )

    assertTrue(
      !config.isEnabled(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES),
      "Should not fail on ignored properties"
    )

    assertTrue(
      config.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT),
      "Should accept empty string as null object"
    )

    // Verify serialization features
    val serConfig = objectMapper.serializationConfig
    assertTrue(
      !serConfig.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
      "Should not write dates as timestamps"
    )

    assertTrue(
      !serConfig.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS),
      "Should not fail on empty beans"
    )

    println("✅ Configuration consistency validated")
    println("Configuration Summary:")
    println("  - Kotlin module: Enhanced with reflection cache and feature flags")
    println("  - Java Time module: Registered for temporal type support")
    println("  - Jimmer module: Registered for ORM entity support")
    println("  - DateTime module: Custom serialization for datetime types")
    println("  - Polymorphic types: Enabled with LaissezFaireSubTypeValidator")
    println("  - Error handling: Lenient configuration for test environments")
  }

  @Test
  @DisplayName("9. Validate Complex Nested Object Serialization")
  fun validateComplexNestedObjectSerialization() {
    data class Address(
      val street: String,
      val city: String,
      val zipCode: String,
    )

    data class Person(
      val id: Long,
      val name: String,
      val email: String?,
      val addresses: List<Address>,
      val metadata: Map<String, Any?>,
      val createdAt: datetime = datetime.now(),
    )

    val person = Person(
      id = 1L,
      name = "John Doe",
      email = "john@example.com",
      addresses = listOf(
        Address("123 Main St", "Anytown", "12345"),
        Address("456 Oak Ave", "Other City", "67890")
      ),
      metadata = mapOf(
        "department" to "Engineering",
        "level" to 5,
        "active" to true,
        "notes" to null
      )
    )

    val json = objectMapper.writeValueAsString(person)
    val deserialized = objectMapper.readValue(json, Person::class.java)

    assertEquals(person.id, deserialized.id)
    assertEquals(person.name, deserialized.name)
    assertEquals(person.email, deserialized.email)
    assertEquals(person.addresses, deserialized.addresses)
    assertEquals(person.metadata, deserialized.metadata)
    assertEquals(person.createdAt, deserialized.createdAt)

    println("✅ Complex nested object serialization validated")
  }

  @Test
  @DisplayName("10. Validate Builder Pattern Support")
  fun validateBuilderPatternSupport() {
    // Test that Jackson can handle Kotlin data classes with default parameters
    data class ConfigurableEntity(
      val id: Long,
      val name: String,
      val enabled: Boolean = true,
      val config: Map<String, String> = emptyMap(),
      val tags: Set<String> = emptySet(),
    )

    // Test with minimal JSON (relying on defaults)
    val minimalJson = """
            {
                "@class": "${ConfigurableEntity::class.java.name}",
                "id": 1,
                "name": "test"
            }
        """.trimIndent()

    val entity = objectMapper.readValue(minimalJson, ConfigurableEntity::class.java)

    assertEquals(1L, entity.id)
    assertEquals("test", entity.name)
    assertEquals(true, entity.enabled) // Default value
    assertEquals(emptyMap<String, String>(), entity.config) // Default value
    assertEquals(emptySet<String>(), entity.tags) // Default value

    println("✅ Builder pattern support validated")
  }
}
