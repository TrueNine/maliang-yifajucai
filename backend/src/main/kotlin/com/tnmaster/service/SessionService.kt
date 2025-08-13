package com.tnmaster.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.tnmaster.config.DatetimeDeserializer
import com.tnmaster.config.DatetimeSerializer
import com.tnmaster.security.SessionData
import com.tnmaster.security.UserPrincipal
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.servlet.deviceId
import io.github.truenine.composeserver.depend.servlet.remoteRequestIp
import io.github.truenine.composeserver.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 基于SessionId的认证服务
 *
 * 使用Redis存储会话信息，集成Casbin权限管理
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Service
class SessionService(
  private val redisTemplate: RedisTemplate<String, Any>,
  private val permissionService: PermissionService,
) {
  companion object {
    @JvmStatic
    private val log = logger<SessionService>()
    private const val SESSION_PREFIX = "session:"
    private const val USER_SESSION_PREFIX = "user:session:"
    private const val USER_DISABLED_PREFIX = "user:disabled:"
    private const val DEFAULT_SESSION_TIMEOUT = 86400L // 24 hours
  }

  /**
   * 当前登录状态
   *
   * @param sessionId session ID
   * @param login 是否已经登录
   * @param sessionTimeout session 剩余有效期
   * @param roles 拥有的角色
   * @param permissions 拥有的权限
   * @param account 账号
   */
  data class SessionView(
    val sessionId: String? = null,
    val login: Boolean = false,
    val sessionTimeout: datetime? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val account: String,
    val userId: RefId,
  )

  /**
   * 生成新的会话ID
   */
  fun generateSessionId(): String {
    return UUID.randomUUID().toString().replace("-", "")
  }

  /**
   * 创建用户会话
   */
  fun createUserSession(
    account: String,
    userId: RefId,
    request: HttpServletRequest,
    roles: Set<String>,
    permissions: Set<String>,
  ): String {
    log.debug("开始创建用户会话: account={}, userId={}", account, userId)

    val sessionId = generateSessionId()
    val loginTime = datetime.now()
    val expireTime = loginTime.plusSeconds(DEFAULT_SESSION_TIMEOUT)

    log.debug("生成会话ID: {}, 过期时间: {}", sessionId, expireTime)

    // 创建会话数据
    val sessionData = SessionData(
      sessionId = sessionId,
      account = account,
      userId = userId,
      deviceId = request.deviceId ?: error("不明设备进行登录"),
      loginIpAddr = request.remoteRequestIp,
      loginTime = loginTime,
      roles = roles,
      permissions = permissions,
      expireTime = expireTime,
      userAgent = request.getHeader("User-Agent"),
      clientVersion = request.getHeader("X-Client-Version")
    )

    log.debug("创建会话数据: deviceId={}, loginIpAddr={}", sessionData.deviceId, sessionData.loginIpAddr)

    // 存储会话信息到 Redis
    val sessionKey = SESSION_PREFIX + sessionId
    log.debug("存储会话到Redis: key={}", sessionKey)

    try {
      redisTemplate.opsForValue().set(sessionKey, sessionData, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)
      log.debug("会话存储成功")

      // 验证存储是否成功
      val storedData = redisTemplate.opsForValue().get(sessionKey)
      log.debug("验证存储结果: {}", if (storedData != null) "存储成功" else "存储失败")

      // 添加更多调试信息
      if (storedData == null) {
        log.error("会话存储验证失败: 无法从Redis读取数据")
        log.debug("原始Redis数据: {}", redisTemplate.opsForValue().get(sessionKey))
        log.debug("Redis键是否存在: {}", redisTemplate.hasKey(sessionKey))
      } else {
        log.debug("存储的数据类型: {}", storedData.javaClass.simpleName)
        log.debug("存储的数据: {}", storedData)
      }

    } catch (e: Exception) {
      log.error("存储会话到Redis失败: key={}, error={}", sessionKey, e.message, e)
      throw e
    }

    // 同时维护用户会话映射（用于管理用户登录状态）
    val userSessionKey = USER_SESSION_PREFIX + account
    log.debug("存储用户会话映射: key={}", userSessionKey)

    try {
      redisTemplate.opsForValue().set(userSessionKey, sessionData, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)
      log.debug("用户会话映射存储成功")
    } catch (e: Exception) {
      log.error("存储用户会话映射失败: key={}, error={}", userSessionKey, e.message, e)
    }

    log.info("用户 {} 创建新会话: {}", account, sessionId)
    return sessionId
  }

  /**
   * 验证会话并获取用户信息
   */
  fun validateSessionAndGetUser(sessionId: String): UserPrincipal? {
    log.debug("开始验证会话: {}", sessionId)

    if (sessionId.isBlank()) {
      log.debug("会话ID为空")
      return null
    }

    val sessionKey = SESSION_PREFIX + sessionId
    log.debug("查找会话键: {}", sessionKey)

    val sessionData = getSessionData(sessionId)
    log.debug("从Redis获取会话数据: {}", if (sessionData != null) "找到" else "未找到")

    if (sessionData == null) {
      log.debug("会话不存在或已过期: {}", sessionId)
      return null
    }

    log.debug(
      "会话数据: account={}, userId={}, expireTime={}",
      sessionData.account, sessionData.userId, sessionData.expireTime
    )

    // 检查会话是否已过期
    if (sessionData.isExpired()) {
      log.debug("会话已过期: {}", sessionId)
      redisTemplate.delete(sessionKey)
      return null
    }

    // 检查用户是否被禁用
    if (isUserDisabled(sessionData.account)) {
      log.warn("用户 {} 已被禁用", sessionData.account)
      return null
    }

    log.debug("开始获取用户权限信息: {}", sessionData.account)

    // 从权限服务获取最新的角色和权限信息
    val currentRoles = try {
      permissionService.getUserRoles(sessionData.account).toSet()
    } catch (e: Exception) {
      log.error("获取用户角色失败: account={}, error={}", sessionData.account, e.message, e)
      return null
    }

    // 获取用户通过角色获得的权限
    val currentPermissions = try {
      val userPermissions = mutableSetOf<String>()
      currentRoles.forEach { role ->
        val rolePermissions = permissionService.getRolePermissions(role)
        rolePermissions.forEach { permission ->
          if (permission.size >= 3 && permission[2] == "allow") {
            // 权限格式: [role, resource, action]
            val role = permission[0]
            val resource = permission[1]
            // 过滤掉角色本身的权限（如 USER, USER, allow），只保留资源权限
            if (role != resource) {
              userPermissions.add(resource)
            }
          }
        }
      }
      userPermissions
    } catch (e: Exception) {
      log.error("获取用户权限失败: account={}, error={}", sessionData.account, e.message, e)
      emptySet()
    }

    log.debug("用户权限信息获取成功: roles={}, permissions={}", currentRoles, currentPermissions)

    val userPrincipal = UserPrincipal(
      userId = sessionData.userId,
      account = sessionData.account,
      nickName = sessionData.nickName,
      loginIpAddr = sessionData.loginIpAddr,
      roles = currentRoles,
      permissions = currentPermissions,
      enabled = !sessionData.disabled,
      nonExpired = true,
      loginTime = sessionData.loginTime,
      deviceId = sessionData.deviceId
    )

    log.debug("会话验证成功，返回用户信息: {}", sessionData.account)
    return userPrincipal
  }

  /**
   * 检查用户是否被禁用
   */
  fun isUserDisabled(account: String): Boolean {
    val disabledKey = USER_DISABLED_PREFIX + account
    return redisTemplate.hasKey(disabledKey)
  }

  /**
   * 清理用户的所有会话
   */
  private fun clearUserSessions(account: String) {
    log.debug("开始清理用户会话: account={}", account)

    // 删除用户会话映射
    val userSessionKey = USER_SESSION_PREFIX + account
    redisTemplate.delete(userSessionKey)
    log.debug("删除用户会话映射: key={}", userSessionKey)

    // 查找并删除所有相关的session
    val sessionKeys = redisTemplate.keys(SESSION_PREFIX + "*")
    log.debug("找到会话键数量: {}", sessionKeys?.size ?: 0)

    sessionKeys?.forEach { sessionKey ->
      val rawData = redisTemplate.opsForValue().get(sessionKey)
      log.debug("检查会话键: {}, 数据类型: {}", sessionKey, rawData?.javaClass?.simpleName)

      // 使用与getSessionData相同的转换逻辑
      val sessionData = when (rawData) {
        is SessionData -> {
          log.debug("直接使用SessionData对象: key={}", sessionKey)
          rawData
        }
        is Map<*, *> -> {
          log.debug("检测到Map类型数据，尝试转换为SessionData: key={}", sessionKey)
          try {
            // 使用ObjectMapper将Map转换为SessionData
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())

            // 创建自定义模块来处理datetime类型
            val datetimeModule = SimpleModule()
            datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
            datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
            objectMapper.registerModule(datetimeModule)

            // 配置忽略未知字段
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val jsonString = objectMapper.writeValueAsString(rawData)
            log.debug("Map转换为JSON成功: key={}, json={}", sessionKey, jsonString.take(200))
            
            val result = objectMapper.readValue(jsonString, SessionData::class.java)
            log.debug("JSON反序列化为SessionData成功: key={}", sessionKey)
            result
          } catch (e: Exception) {
            log.error("转换SessionData失败: key={}, error={}", sessionKey, e.message, e)
            null
          }
        }
        else -> {
          log.debug("未知数据类型: key={}, type={}", sessionKey, rawData?.javaClass?.simpleName)
          null
        }
      }

      if (sessionData?.account == account) {
        log.debug("删除用户会话: key={}, account={}", sessionKey, account)
        redisTemplate.delete(sessionKey)
      }
    }

    log.debug("用户会话清理完成: account={}", account)
  }

  /**
   * 强制将用户踢出并封禁一段时间
   */
  fun disableUser(account: String, duration: Duration = Duration.ofDays(1)) {
    // 清理用户会话
    clearUserSessions(account)

    // 设置禁用标记
    val disabledKey = USER_DISABLED_PREFIX + account
    redisTemplate.opsForValue().set(disabledKey, true, duration.seconds, TimeUnit.SECONDS)

    log.info("用户 {} 已被禁用 {} 秒", account, duration.seconds)
  }

  /**
   * 用户登出
   */
  fun logout(sessionId: String) {
    val sessionKey = SESSION_PREFIX + sessionId
    val sessionData = redisTemplate.opsForValue().get(sessionKey) as? SessionData
    val account = sessionData?.account

    // 删除会话
    redisTemplate.delete(sessionKey)

    // 删除用户会话映射
    if (account != null) {
      val userSessionKey = USER_SESSION_PREFIX + account
      redisTemplate.delete(userSessionKey)
      log.info("用户 {} 已登出，会话ID: {}", account, sessionId)
    }
  }

  /**
   * 用户登出（通过账号）
   */
  fun logoutByAccount(account: String) {
    // 清理用户会话
    clearUserSessions(account)

    log.info("用户 {} 已登出", account)
  }

  /**
   * 刷新会话过期时间
   */
  fun refreshSession(sessionId: String): Boolean {
    log.debug("开始刷新会话: {}", sessionId)

    val sessionKey = SESSION_PREFIX + sessionId
    log.debug("查找会话键: {}", sessionKey)

    val rawData = redisTemplate.opsForValue().get(sessionKey)
    log.debug("从Redis获取原始数据: {}", rawData)
    log.debug("原始数据类型: {}", rawData?.javaClass?.simpleName)

    // 尝试转换为SessionData
    val sessionData = when (rawData) {
      is SessionData -> {
        log.debug("直接使用SessionData对象")
        rawData
      }
      is Map<*, *> -> {
        log.debug("检测到Map类型数据，尝试转换为SessionData")
        try {
          // 使用ObjectMapper将Map转换为SessionData
          val objectMapper = ObjectMapper()
          objectMapper.registerModule(JavaTimeModule())

          // 创建自定义模块来处理datetime类型
          val datetimeModule = SimpleModule()
          datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
          datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
          objectMapper.registerModule(datetimeModule)

          // 配置忽略未知字段
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

          val jsonString = objectMapper.writeValueAsString(rawData)
          log.debug("Map转换为JSON成功: {}", jsonString.take(200))
          
          val result = objectMapper.readValue(jsonString, SessionData::class.java)
          log.debug("JSON反序列化为SessionData成功")
          result
        } catch (e: Exception) {
          log.error("转换SessionData失败: error={}", e.message, e)
          null
        }
      }
      else -> {
        log.debug("未知数据类型: {}", rawData?.javaClass?.simpleName)
        null
      }
    }

    log.debug("转换后的会话数据: {}", if (sessionData != null) "成功" else "失败")

    if (sessionData == null) {
      log.debug("会话数据为空，刷新失败: {}", sessionId)
      return false
    }

    log.debug(
      "会话数据: account={}, userId={}, expireTime={}",
      sessionData.account, sessionData.userId, sessionData.expireTime
    )

    // 创建新的过期时间
    val newExpireTime = datetime.now().plusSeconds(DEFAULT_SESSION_TIMEOUT)
    val updatedSessionData = sessionData.copy(expireTime = newExpireTime)

    log.debug("更新会话过期时间: {} -> {}", sessionData.expireTime, newExpireTime)

    // 更新会话数据
    redisTemplate.opsForValue().set(sessionKey, updatedSessionData, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)
    log.debug("会话数据更新成功")

    // 同时刷新用户会话映射
    val userSessionKey = USER_SESSION_PREFIX + sessionData.account
    redisTemplate.opsForValue().set(userSessionKey, updatedSessionData, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)
    log.debug("用户会话映射更新成功")

    log.debug("会话刷新完成: {}", sessionId)
    return true
  }

  /**
   * 获取会话剩余时间（秒）
   */
  fun getSessionRemainingTime(sessionId: String): Long {
    val sessionKey = SESSION_PREFIX + sessionId
    val sessionData = redisTemplate.opsForValue().get(sessionKey) as? SessionData

    if (sessionData == null) {
      return 0
    }

    return sessionData.getRemainingTime().seconds.coerceAtLeast(0)
  }

  /**
   * 检查会话是否存在
   */
  fun sessionExists(sessionId: String): Boolean {
    val sessionKey = SESSION_PREFIX + sessionId
    return redisTemplate.hasKey(sessionKey)
  }

  /**
   * 获取会话数据
   */
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
        try {
          // 使用ObjectMapper将Map转换为SessionData
          val objectMapper = ObjectMapper()
          objectMapper.registerModule(JavaTimeModule())

          // 创建自定义模块来处理datetime类型
          val datetimeModule = SimpleModule()
          datetimeModule.addSerializer(datetime::class.java, DatetimeSerializer())
          datetimeModule.addDeserializer(datetime::class.java, DatetimeDeserializer())
          objectMapper.registerModule(datetimeModule)

          // 配置忽略未知字段
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

          val jsonString = objectMapper.writeValueAsString(rawData)
          log.debug("Map转换为JSON成功: {}", jsonString.take(200))
          
          val result = objectMapper.readValue(jsonString, SessionData::class.java)
          log.debug("JSON反序列化为SessionData成功")
          result
        } catch (e: Exception) {
          log.error("转换SessionData失败: key={}, error={}", sessionKey, e.message, e)
          null
        }
      }
      else -> {
        log.debug("未知数据类型: {}", rawData?.javaClass?.simpleName)
        null
      }
    }
  }

  /**
   * 获取用户当前会话
   */
  fun getUserSession(account: String): SessionData? {
    val userSessionKey = USER_SESSION_PREFIX + account
    return redisTemplate.opsForValue().get(userSessionKey) as? SessionData
  }
}
