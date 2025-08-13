# Redis序列化问题修复指南

## 问题描述

在测试过程中遇到以下错误：

```
Could not read JSON:Could not resolve subtype of [simple type, class java.lang.Object]: missing type id property '@class'
```

这是一个典型的Jackson JSON反序列化问题，发生在Redis存储和读取SessionData对象时。

## 问题分析

### 根本原因

1. **类型信息丢失**：Redis中存储的对象缺少`@class`属性，导致反序列化时无法确定目标类型
2. **配置不一致**：生产环境和测试环境的Redis序列化配置存在差异
3. **序列化器选择**：使用了`GenericJackson2JsonRedisSerializer`但没有正确配置类型信息处理

### 错误流程

1. 序列化成功：`SessionData`对象被正确序列化并存储到Redis
2. 反序列化失败：从Redis读取时，由于缺少`@class`属性，Jackson无法确定目标类型
3. 类型转换失败：尝试将`Map`转换为`SessionData`时出现异常

## 解决方案

### 1. 统一Redis配置

修改`RedisConfig.kt`，确保生产环境和测试环境使用相同的序列化配置：

```kotlin
@Bean("redisTemplate")
@Primary
fun redisTemplate(
  connectionFactory: RedisConnectionFactory,
  redisSerializationErrorHandler: RedisSerializationErrorHandler
): RedisTemplate<String, Any> {
  val template = RedisTemplate<String, Any>()
  template.connectionFactory = connectionFactory

  // 创建统一的ObjectMapper用于Redis序列化
  val objectMapper = createRedisObjectMapper()

  // 使用带错误处理的Redis序列化器
  val jsonSerializer = ErrorHandlingRedisSerializer(objectMapper, redisSerializationErrorHandler)

  // 设置序列化器
  template.keySerializer = StringRedisSerializer()
  template.valueSerializer = jsonSerializer
  template.hashKeySerializer = StringRedisSerializer()
  template.hashValueSerializer = jsonSerializer

  template.afterPropertiesSet()
  return template
}
```

### 2. 配置类型信息处理

在`createRedisObjectMapper()`方法中启用类型信息：

```kotlin
private fun createRedisObjectMapper(): ObjectMapper {
  return ObjectMapper().apply {
    // 注册必要的模块
    registerModule(KotlinModule.Builder().build())
    registerModule(JavaTimeModule())
    registerModule(ImmutableModule())
    
    // 创建自定义模块来处理datetime类型
    val datetimeModule = SimpleModule()
    datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
    datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
    registerModule(datetimeModule)

    // 关键配置：启用类型信息处理
    activateDefaultTyping(
      LaissezFaireSubTypeValidator.instance,
      ObjectMapper.DefaultTyping.NON_FINAL,
      JsonTypeInfo.As.PROPERTY
    )
  }
}
```

### 3. 使用错误处理序列化器

采用`ErrorHandlingRedisSerializer`来处理序列化异常：

```kotlin
// 使用带错误处理的Redis序列化器
val jsonSerializer = ErrorHandlingRedisSerializer(objectMapper, redisSerializationErrorHandler)
```

### 4. 增强日志记录

在`SessionService`中添加详细的调试日志：

```kotlin
fun getSessionData(sessionId: String): SessionData? {
  val sessionKey = SESSION_PREFIX + sessionId
  val rawData = redisTemplate.opsForValue().get(sessionKey)
  
  log.debug("从Redis获取原始数据: key={}, 数据类型: {}", sessionKey, rawData?.javaClass?.simpleName)
  
  return when (rawData) {
    is SessionData -> {
      log.debug("直接返回SessionData对象")
      rawData
    }
    is Map<*, *> -> {
      log.debug("检测到Map类型数据，尝试转换为SessionData")
      // ... 转换逻辑
    }
    else -> {
      log.debug("未知数据类型: {}", rawData?.javaClass?.simpleName)
      null
    }
  }
}
```

## 配置要点

### 类型信息配置

- **`activateDefaultTyping`**：启用类型信息序列化
- **`LaissezFaireSubTypeValidator.instance`**：允许所有类型
- **`ObjectMapper.DefaultTyping.NON_FINAL`**：为非final类添加类型信息
- **`JsonTypeInfo.As.PROPERTY`**：将类型信息作为属性存储

### 序列化器选择

- **`ErrorHandlingRedisSerializer`**：提供错误处理和恢复机制
- **`Jackson2JsonRedisSerializer`**：避免类型信息问题
- **避免使用**：`GenericJackson2JsonRedisSerializer`（可能导致类型信息丢失）

### 模块注册顺序

1. `KotlinModule`：支持Kotlin特性
2. `JavaTimeModule`：支持时间类型
3. `ImmutableModule`：支持Jimmer实体
4. 自定义模块：处理特定类型

## 验证方法

### 1. 检查Redis存储

```bash
# 查看Redis中存储的数据
redis-cli get "session:your-session-id"
```

确保数据包含`@class`属性：

```json
{
  "@class": "com.tnmaster.security.SessionData",
  "sessionId": "...",
  "account": "...",
  // ... 其他属性
}
```

### 2. 测试序列化/反序列化

```kotlin
@Test
fun test_redis_serialization_works_correctly() {
  // 创建测试数据
  val sessionData = SessionData(...)
  
  // 存储到Redis
  redisTemplate.opsForValue().set("test:session", sessionData)
  
  // 从Redis读取
  val retrieved = redisTemplate.opsForValue().get("test:session")
  
  // 验证类型和内容
  assertTrue(retrieved is SessionData)
  assertEquals(sessionData.sessionId, retrieved.sessionId)
}
```

### 3. 检查日志输出

确保日志中显示正确的数据类型：

```
DEBUG - 从Redis获取原始数据: key=session:xxx, 数据类型=SessionData
DEBUG - 直接返回SessionData对象
```

而不是：

```
DEBUG - 从Redis获取原始数据: key=session:xxx, 数据类型=LinkedHashMap
DEBUG - 检测到Map类型数据，尝试转换为SessionData
```

## 常见问题

### Q: 为什么会出现Map类型数据？

A: 当Redis序列化配置不正确时，对象可能被序列化为Map而不是原始类型。

### Q: 如何避免类型信息丢失？

A: 确保使用正确的序列化器配置，并启用`activateDefaultTyping`。

### Q: 测试环境和生产环境配置不一致怎么办？

A: 使用`@Primary`注解确保测试配置覆盖生产配置，或统一两套环境的配置。

## 总结

通过以上配置，可以解决Redis序列化中的类型信息丢失问题，确保：

1. **类型安全**：对象在Redis中保持正确的类型信息
2. **一致性**：生产环境和测试环境使用相同的序列化策略
3. **错误处理**：提供优雅的错误恢复机制
4. **调试友好**：详细的日志记录便于问题排查

关键是要确保Jackson正确配置类型信息处理，并使用合适的Redis序列化器。
