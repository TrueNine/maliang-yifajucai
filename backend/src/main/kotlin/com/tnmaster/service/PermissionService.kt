package com.tnmaster.service

import io.github.truenine.composeserver.logger
import org.casbin.jcasbin.main.Enforcer
import org.springframework.stereotype.Service

/**
 * 权限管理服务
 *
 * 基于Casbin实现RBAC权限控制
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Service
class PermissionService(
  private val enforcer: Enforcer
) {
  companion object {
    private val log = logger<PermissionService>()
  }

  /**
   * 检查用户是否有指定权限
   *
   * @param account 用户账号
   * @param resource 资源
   * @param action 操作
   * @return 是否有权限
   */
  fun checkPermission(account: String, resource: String, action: String): Boolean {
    return try {
      val hasPermission = enforcer.enforce(account, resource, action)
      log.debug("权限检查: 用户={}, 资源={}, 操作={}, 结果={}", account, resource, action, hasPermission)
      hasPermission
    } catch (e: Exception) {
      log.error("权限检查失败: 用户={}, 资源={}, 操作={}", account, resource, action, e)
      false
    }
  }

  /**
   * 检查用户是否有指定角色
   *
   * @param account 用户账号
   * @param role 角色
   * @return 是否有角色
   */
  fun hasRole(account: String, role: String): Boolean {
    return try {
      val hasRole = enforcer.hasRoleForUser(account, role)
      log.debug("角色检查: 用户={}, 角色={}, 结果={}", account, role, hasRole)
      hasRole
    } catch (e: Exception) {
      log.error("角色检查失败: 用户={}, 角色={}", account, role, e)
      false
    }
  }

  /**
   * 获取用户所有角色
   *
   * @param account 用户账号
   * @return 角色列表
   */
  fun getUserRoles(account: String): List<String> {
    return try {
      val roles = enforcer.getRolesForUser(account)
      log.debug("获取用户角色: 用户={}, 角色={}", account, roles)
      roles
    } catch (e: Exception) {
      log.error("获取用户角色失败: 用户={}", account, e)
      emptyList()
    }
  }

  /**
   * 获取用户所有权限
   *
   * @param account 用户账号
   * @return 权限列表
   */
  fun getUserPermissions(account: String): List<List<String>> {
    return try {
      val permissions = enforcer.getPermissionsForUser(account)
      log.debug("获取用户权限: 用户={}, 权限数量={}", account, permissions.size)
      permissions
    } catch (e: Exception) {
      log.error("获取用户权限失败: 用户={}", account, e)
      emptyList()
    }
  }

  /**
   * 添加用户角色
   *
   * @param account 用户账号
   * @param role 角色
   * @return 是否成功
   */
  fun addRoleForUser(account: String, role: String): Boolean {
    return try {
      val result = enforcer.addRoleForUser(account, role)
      if (result) {
        log.info("为用户添加角色成功: 用户={}, 角色={}", account, role)
      } else {
        log.warn("为用户添加角色失败: 用户={}, 角色={}", account, role)
      }
      result
    } catch (e: Exception) {
      log.error("为用户添加角色异常: 用户={}, 角色={}", account, role, e)
      false
    }
  }

  /**
   * 移除用户角色
   *
   * @param account 用户账号
   * @param role 角色
   * @return 是否成功
   */
  fun removeRoleForUser(account: String, role: String): Boolean {
    return try {
      val result = enforcer.deleteRoleForUser(account, role)
      if (result) {
        log.info("移除用户角色成功: 用户={}, 角色={}", account, role)
      } else {
        log.warn("移除用户角色失败: 用户={}, 角色={}", account, role)
      }
      result
    } catch (e: Exception) {
      log.error("移除用户角色异常: 用户={}, 角色={}", account, role, e)
      false
    }
  }

  /**
   * 添加权限策略
   *
   * @param role 角色
   * @param resource 资源
   * @param action 操作
   * @return 是否成功
   */
  fun addPolicy(role: String, resource: String, action: String): Boolean {
    return try {
      val result = enforcer.addPolicy(role, resource, action)
      if (result) {
        log.info("添加权限策略成功: 角色={}, 资源={}, 操作={}", role, resource, action)
      } else {
        log.warn("添加权限策略失败: 角色={}, 资源={}, 操作={}", role, resource, action)
      }
      result
    } catch (e: Exception) {
      log.error("添加权限策略异常: 角色={}, 资源={}, 操作={}", role, resource, action, e)
      false
    }
  }

  /**
   * 移除权限策略
   *
   * @param role 角色
   * @param resource 资源
   * @param action 操作
   * @return 是否成功
   */
  fun removePolicy(role: String, resource: String, action: String): Boolean {
    return try {
      val result = enforcer.removePolicy(role, resource, action)
      if (result) {
        log.info("移除权限策略成功: 角色={}, 资源={}, 操作={}", role, resource, action)
      } else {
        log.warn("移除权限策略失败: 角色={}, 资源={}, 操作={}", role, resource, action)
      }
      result
    } catch (e: Exception) {
      log.error("移除权限策略异常: 角色={}, 资源={}, 操作={}", role, resource, action, e)
      false
    }
  }

  /**
   * 获取所有策略
   *
   * @return 策略列表
   */
  fun getAllPolicies(): List<List<String>> {
    return try {
      val policies = enforcer.policy
      log.debug("获取所有策略: 数量={}", policies.size)
      policies
    } catch (e: Exception) {
      log.error("获取所有策略失败", e)
      emptyList()
    }
  }

  /**
   * 重新加载策略
   *
   * @return 是否成功
   */
  fun reloadPolicy(): Boolean {
    return try {
      enforcer.loadPolicy()
      log.info("重新加载策略成功")
      true
    } catch (e: Exception) {
      log.error("重新加载策略失败", e)
      false
    }
  }

  /**
   * 创建角色
   *
   * @param roleName 角色名称
   * @param description 角色描述
   * @return 是否成功
   */
  fun createRole(roleName: String, description: String): Boolean {
    return try {
      // Casbin本身不直接支持角色创建，这里我们通过添加一个特殊的策略来标记角色存在
      val result = enforcer.addPolicy("role:$roleName", "meta", "exists")
      if (result) {
        log.info("创建角色成功: 角色={}, 描述={}", roleName, description)
      } else {
        log.warn("创建角色失败: 角色={}, 描述={}", roleName, description)
      }
      result
    } catch (e: Exception) {
      log.error("创建角色异常: 角色={}, 描述={}", roleName, description, e)
      false
    }
  }

  /**
   * 删除角色
   *
   * @param roleName 角色名称
   * @return 是否成功
   */
  fun deleteRole(roleName: String): Boolean {
    return try {
      // 删除角色的所有策略
      val result1 = enforcer.removeFilteredPolicy(0, roleName)
      // 删除角色标记
      val result2 = enforcer.removePolicy("role:$roleName", "meta", "exists")
      // 删除所有用户的该角色
      val usersWithRole = enforcer.getUsersForRole(roleName)
      usersWithRole.forEach { user ->
        enforcer.deleteRoleForUser(user, roleName)
      }

      val success = result1 || result2
      if (success) {
        log.info("删除角色成功: 角色={}", roleName)
      } else {
        log.warn("删除角色失败: 角色={}", roleName)
      }
      success
    } catch (e: Exception) {
      log.error("删除角色异常: 角色={}", roleName, e)
      false
    }
  }

  /**
   * 获取所有角色
   *
   * @return 角色列表
   */
  fun getAllRoles(): List<Map<String, Any>> {
    return try {
      val allRoles = enforcer.allRoles
      val roleList = allRoles.map { role ->
        mapOf(
          "name" to role,
          "description" to "角色: $role"
        )
      }
      log.debug("获取所有角色: 数量={}", roleList.size)
      roleList
    } catch (e: Exception) {
      log.error("获取所有角色失败", e)
      emptyList()
    }
  }

  /**
   * 获取角色信息
   *
   * @param roleName 角色名称
   * @return 角色信息
   */
  fun getRoleInfo(roleName: String): Map<String, Any>? {
    return try {
      val allRoles = enforcer.allRoles
      if (!allRoles.contains(roleName)) {
        return null
      }

      val permissions = getRolePermissions(roleName)
      val users = enforcer.getUsersForRole(roleName)

      mapOf(
        "name" to roleName,
        "description" to "角色: $roleName",
        "permissions" to permissions.map { "${it[1]}:${it[2]}" },
        "users" to users
      )
    } catch (e: Exception) {
      log.error("获取角色信息失败: 角色={}", roleName, e)
      null
    }
  }

  /**
   * 获取角色权限
   *
   * @param roleName 角色名称
   * @return 权限列表
   */
  fun getRolePermissions(roleName: String): List<List<String>> {
    return try {
      val permissions = enforcer.getPermissionsForUser(roleName)
      log.debug("获取角色权限: 角色={}, 权限数量={}", roleName, permissions.size)
      permissions
    } catch (e: Exception) {
      log.error("获取角色权限失败: 角色={}", roleName, e)
      emptyList()
    }
  }
}
