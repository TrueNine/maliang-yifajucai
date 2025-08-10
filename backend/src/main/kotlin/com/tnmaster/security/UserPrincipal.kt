package com.tnmaster.security

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime

/**
 * # 用户认证主体信息
 *
 * @author TrueNine
 * @since 2025-01-10
 */
data class UserPrincipal(
  val userId: RefId,
  val account: String,
  val nickName: String?,
  val loginIpAddr: String?,
  val roles: Set<String> = emptySet(),
  val permissions: Set<String> = emptySet(),
  val enabled: Boolean = true,
  val nonExpired: Boolean = true,
  val loginTime: datetime? = null,
  val deviceId: String? = null
) {
  
  /**
   * 检查是否拥有指定权限
   */
  fun hasPermission(permission: String): Boolean {
    return permissions.contains(permission)
  }

  /**
   * 检查是否拥有指定角色
   */
  fun hasRole(role: String): Boolean {
    return roles.contains(role)
  }

  /**
   * 检查是否拥有任意一个指定权限
   */
  fun hasAnyPermission(vararg permissions: String): Boolean {
    return permissions.any { this.permissions.contains(it) }
  }

  /**
   * 检查是否拥有任意一个指定角色
   */
  fun hasAnyRole(vararg roles: String): Boolean {
    return roles.any { this.roles.contains(it) }
  }
}
