package com.tnmaster.service

import com.tnmaster.repositories.IUserAccountRepo
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
 * # 基于SessionId的认证服务
 *
 * 替代JWT认证，使用简单的sessionId进行用户认证
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Service
class SessionService(
  private val userAccountRepo: IUserAccountRepo,
  private val redisTemplate: RedisTemplate<String, Any>,
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
   * # 当前登录状态
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
  fun createUserSession(account: String, userId: RefId, request: HttpServletRequest, roles: Set<String>, permissions: Set<String>): String {
    val sessionId = generateSessionId()
    val loginTime = datetime.now()

    // 存储会话信息到 Redis
    val sessionKey = SESSION_PREFIX + sessionId
    val sessionData = mapOf<String, Any>(
      "account" to account,
      "userId" to userId.toString(),
      "deviceId" to (request.deviceId ?: error("不明设备进行登录")),
      "loginIpAddr" to request.remoteRequestIp,
      "loginTime" to loginTime.toString(),
      "roles" to roles.joinToString(","),
      "permissions" to permissions.joinToString(","),
      "disabled" to false
    )

    redisTemplate.opsForHash<String, Any>().putAll(sessionKey, sessionData)
    redisTemplate.expire(sessionKey, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)

    // 同时维护用户会话映射（用于管理用户登录状态）
    val userSessionKey = USER_SESSION_PREFIX + account
    redisTemplate.opsForHash<String, Any>().putAll(userSessionKey, sessionData)
    redisTemplate.expire(userSessionKey, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)

    log.info("用户 {} 创建新会话: {}", account, sessionId)
    return sessionId
  }

  /**
   * 验证会话并获取用户信息
   */
  fun validateSessionAndGetUser(sessionId: String): UserPrincipal? {
    if (sessionId.isBlank()) {
      return null
    }

    val sessionKey = SESSION_PREFIX + sessionId
    val sessionData = redisTemplate.opsForHash<String, Any>().entries(sessionKey)

    if (sessionData.isEmpty()) {
      log.debug("会话不存在或已过期: {}", sessionId)
      return null
    }

    val account = sessionData["account"] as? String ?: return null
    val userIdStr = sessionData["userId"] as? String ?: return null

    // 检查用户是否被禁用
    if (isUserDisabled(account)) {
      log.warn("用户 {} 已被禁用", account)
      return null
    }

    // 解析用户ID
    val userId = try {
      userIdStr.toLong()
    } catch (e: Exception) {
      log.warn("无法解析用户ID: {}", userIdStr, e)
      return null
    }

    return UserPrincipal(
      userId = userId,
      account = account,
      nickName = sessionData["nickName"] as? String,
      loginIpAddr = sessionData["loginIpAddr"] as? String,
      roles = (sessionData["roles"] as? String)?.split(",")?.toSet() ?: emptySet(),
      permissions = (sessionData["permissions"] as? String)?.split(",")?.toSet() ?: emptySet(),
      enabled = !(sessionData["disabled"] as? Boolean ?: false),
      nonExpired = true,
      loginTime = sessionData["loginTime"]?.let { datetime.parse(it.toString()) },
      deviceId = sessionData["deviceId"] as? String
    )
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
    // 删除用户会话映射
    val userSessionKey = USER_SESSION_PREFIX + account
    redisTemplate.delete(userSessionKey)

    // 查找并删除所有相关的session
    val sessionKeys = redisTemplate.keys(SESSION_PREFIX + "*")
    sessionKeys?.forEach { sessionKey ->
      val sessionData = redisTemplate.opsForHash<String, Any>().entries(sessionKey)
      if (sessionData["account"] == account) {
        redisTemplate.delete(sessionKey)
      }
    }
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
    val sessionData = redisTemplate.opsForHash<String, Any>().entries(sessionKey)
    val account = sessionData["account"] as? String

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
    val sessionKey = SESSION_PREFIX + sessionId
    if (!redisTemplate.hasKey(sessionKey)) {
      return false
    }

    redisTemplate.expire(sessionKey, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)

    // 同时刷新用户会话映射
    val sessionData = redisTemplate.opsForHash<String, Any>().entries(sessionKey)
    val account = sessionData["account"] as? String
    if (account != null) {
      val userSessionKey = USER_SESSION_PREFIX + account
      redisTemplate.expire(userSessionKey, DEFAULT_SESSION_TIMEOUT, TimeUnit.SECONDS)
    }

    return true
  }

  /**
   * 获取会话剩余时间（秒）
   */
  fun getSessionRemainingTime(sessionId: String): Long {
    val sessionKey = SESSION_PREFIX + sessionId
    return redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS)
  }

  /**
   * 检查会话是否存在
   */
  fun sessionExists(sessionId: String): Boolean {
    val sessionKey = SESSION_PREFIX + sessionId
    return redisTemplate.hasKey(sessionKey)
  }
}
