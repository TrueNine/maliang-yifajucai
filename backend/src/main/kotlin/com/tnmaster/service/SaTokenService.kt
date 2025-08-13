package com.tnmaster.service

import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.security.UserPrincipal
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class AuthService(
  private val userAccountRepo: IUserAccountRepo,
  private val sessionService: SessionService,
  private val redisTemplate: RedisTemplate<String, Any>,
) {
  companion object {
    private val log = logger<AuthService>()
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
  data class AuthTokenView(
    val sessionId: String? = null,
    val login: Boolean = false,
    val sessionTimeout: datetime? = null,
    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),
    val account: String,
    val userId: RefId,
  )

  /**
   * 验证 sessionId 并获取用户信息
   */
  fun validateSessionAndGetUser(sessionId: String): UserPrincipal? {
    return sessionService.validateSessionAndGetUser(sessionId)
  }

  /**
   * 检查用户是否被禁用
   */
  fun isUserDisabled(account: String): Boolean {
    return sessionService.isUserDisabled(account)
  }

  /**
   * 设置用户登录状态
   */
  fun setUserLoginState(account: String, userId: RefId, request: HttpServletRequest, roles: Set<String>, permissions: Set<String>): String {
    // 使用 SessionService 创建会话
    return sessionService.createUserSession(account, userId, request, roles, permissions)
  }

  /**
   * 强制将用户踢出并封禁一段时间
   */
  fun disableUser(account: String, duration: Duration = Duration.ofDays(1)) {
    sessionService.disableUser(account, duration)
  }

  /**
   * 用户登出（通过账号）
   */
  fun logoutByAccount(account: String) {
    sessionService.logoutByAccount(account)
  }

  /**
   * 用户登出（通过sessionId）
   */
  fun logoutBySessionId(sessionId: String) {
    sessionService.logout(sessionId)
  }

  /**
   * 获取当前登录用户信息
   */
  fun getCurrentUser(): UserPrincipal? {
    return com.tnmaster.security.UserContextHolder.getCurrentUser()
  }

  /**
   * 更新用户最后登录时间
   */
  fun updateLastLoginTime(account: String) {
    userAccountRepo.updateLastLoginTimeByAccount(account, datetime.now())
  }
}
