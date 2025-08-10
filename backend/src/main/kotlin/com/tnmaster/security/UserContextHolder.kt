package com.tnmaster.security

/**
 * # 用户上下文持有者
 * 
 * 使用 ThreadLocal 存储当前请求的用户信息
 *
 * @author TrueNine
 * @since 2025-01-10
 */
object UserContextHolder {
  private val userContext = ThreadLocal<UserPrincipal>()

  /**
   * 设置当前用户
   */
  fun setCurrentUser(user: UserPrincipal) {
    userContext.set(user)
  }

  /**
   * 获取当前用户
   */
  fun getCurrentUser(): UserPrincipal? {
    return userContext.get()
  }

  /**
   * 清除当前用户上下文
   */
  fun clear() {
    userContext.remove()
  }

  /**
   * 检查当前用户是否已登录
   */
  fun isAuthenticated(): Boolean {
    return getCurrentUser() != null
  }

  /**
   * 获取当前用户账号
   */
  fun getCurrentAccount(): String? {
    return getCurrentUser()?.account
  }

  /**
   * 获取当前用户ID
   */
  fun getCurrentUserId(): io.github.truenine.composeserver.RefId? {
    return getCurrentUser()?.userId
  }
}
