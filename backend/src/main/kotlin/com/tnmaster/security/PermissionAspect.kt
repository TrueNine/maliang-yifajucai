package com.tnmaster.security

import com.tnmaster.service.PermissionService
import io.github.truenine.composeserver.logger
import org.springframework.stereotype.Component

/**
 * 权限检查服务
 *
 * 基于注解进行权限验证，集成Casbin权限管理
 *
 * @author TrueNine
 * @since 2025-01-11
 */
@Component
class PermissionAspect(
  private val permissionService: PermissionService
) {

  companion object {
    private val log = logger<PermissionAspect>()
  }

  /**
   * 检查用户是否已登录
   */
  fun checkLogin(): Boolean {
    val currentUser = UserContextHolder.getCurrentUser()
    return currentUser != null && currentUser.enabled && currentUser.nonExpired
  }

  /**
   * 检查用户权限
   */
  fun checkPermission(permission: String): Boolean {
    val currentUser = UserContextHolder.getCurrentUser() ?: return false
    val parts = permission.split(":")
    if (parts.size != 2) return false

    return permissionService.checkPermission(currentUser.account, parts[0], parts[1])
  }

  /**
   * 检查用户角色
   */
  fun checkRole(role: String): Boolean {
    val currentUser = UserContextHolder.getCurrentUser() ?: return false
    return permissionService.hasRole(currentUser.account, role)
  }


}
