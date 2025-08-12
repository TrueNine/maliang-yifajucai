package com.tnmaster.apis

import com.tnmaster.service.PermissionService
import io.github.truenine.composeserver.logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 权限管理API接口
 *
 * 提供角色和权限的管理功能
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@RestController
@RequestMapping("/api/permissions")
class PermissionManagementApi(
  private val permissionService: PermissionService
) {
  companion object {
    private val log = logger<PermissionManagementApi>()
  }

  /**
   * 检查用户权限
   */
  @PostMapping("/check")
  fun checkPermission(
    @RequestParam account: String,
    @RequestParam resource: String,
    @RequestParam action: String
  ): ResponseEntity<Map<String, Any>> {
    val hasPermission = permissionService.checkPermission(account, resource, action)
    return ResponseEntity.ok(mapOf(
      "hasPermission" to hasPermission,
      "account" to account,
      "resource" to resource,
      "action" to action
    ))
  }

  /**
   * 检查用户角色
   */
  @PostMapping("/check-role")
  fun checkRole(
    @RequestParam account: String,
    @RequestParam role: String
  ): ResponseEntity<Map<String, Any>> {
    val hasRole = permissionService.hasRole(account, role)
    return ResponseEntity.ok(mapOf(
      "hasRole" to hasRole,
      "account" to account,
      "role" to role
    ))
  }

  /**
   * 获取用户所有角色
   */
  @GetMapping("/user/{account}/roles")
  fun getUserRoles(@PathVariable account: String): ResponseEntity<Map<String, Any>> {
    val roles = permissionService.getUserRoles(account)
    return ResponseEntity.ok(mapOf(
      "account" to account,
      "roles" to roles
    ))
  }

  /**
   * 获取用户所有权限
   */
  @GetMapping("/user/{account}/permissions")
  fun getUserPermissions(@PathVariable account: String): ResponseEntity<Map<String, Any>> {
    val permissions = permissionService.getUserPermissions(account)
    return ResponseEntity.ok(mapOf(
      "account" to account,
      "permissions" to permissions
    ))
  }

  /**
   * 为用户添加角色
   */
  @PostMapping("/user/role")
  fun addRoleForUser(
    @RequestParam account: String,
    @RequestParam role: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.addRoleForUser(account, role)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "account" to account,
      "role" to role
    ))
  }

  /**
   * 移除用户角色
   */
  @DeleteMapping("/user/role")
  fun removeRoleForUser(
    @RequestParam account: String,
    @RequestParam role: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.removeRoleForUser(account, role)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "account" to account,
      "role" to role
    ))
  }

  /**
   * 添加权限策略
   */
  @PostMapping("/policy")
  fun addPolicy(
    @RequestParam role: String,
    @RequestParam resource: String,
    @RequestParam action: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.addPolicy(role, resource, action)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "role" to role,
      "resource" to resource,
      "action" to action
    ))
  }

  /**
   * 移除权限策略
   */
  @DeleteMapping("/policy")
  fun removePolicy(
    @RequestParam role: String,
    @RequestParam resource: String,
    @RequestParam action: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.removePolicy(role, resource, action)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "role" to role,
      "resource" to resource,
      "action" to action
    ))
  }

  /**
   * 获取所有策略
   */
  @GetMapping("/policies")
  fun getAllPolicies(): ResponseEntity<Map<String, Any>> {
    val policies = permissionService.getAllPolicies()
    return ResponseEntity.ok(mapOf(
      "policies" to policies,
      "count" to policies.size
    ))
  }

  /**
   * 重新加载策略
   */
  @PostMapping("/reload")
  fun reloadPolicy(): ResponseEntity<Map<String, Any>> {
    val success = permissionService.reloadPolicy()
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "message" to if (success) "策略重新加载成功" else "策略重新加载失败"
    ))
  }

  // ========== 角色管理API ==========

  /**
   * 创建角色
   */
  @PostMapping("/roles")
  fun createRole(
    @RequestParam name: String,
    @RequestParam(required = false, defaultValue = "") description: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.createRole(name, description)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "roleName" to name,
      "description" to description,
      "message" to if (success) "角色创建成功" else "角色创建失败"
    ))
  }

  /**
   * 删除角色
   */
  @DeleteMapping("/roles/{roleName}")
  fun deleteRole(@PathVariable roleName: String): ResponseEntity<Map<String, Any>> {
    val success = permissionService.deleteRole(roleName)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "roleName" to roleName,
      "message" to if (success) "角色删除成功" else "角色删除失败"
    ))
  }

  /**
   * 获取所有角色
   */
  @GetMapping("/roles")
  fun getAllRoles(): ResponseEntity<Map<String, Any>> {
    val roles = permissionService.getAllRoles()
    return ResponseEntity.ok(mapOf(
      "roles" to roles,
      "count" to roles.size
    ))
  }

  /**
   * 获取角色详情
   */
  @GetMapping("/roles/{roleName}")
  fun getRoleInfo(@PathVariable roleName: String): ResponseEntity<Map<String, Any>> {
    val roleInfo = permissionService.getRoleInfo(roleName)
    return if (roleInfo != null) {
      ResponseEntity.ok(roleInfo)
    } else {
      ResponseEntity.status(404).body(mapOf(
        "error" to "角色不存在",
        "roleName" to roleName
      ))
    }
  }

  /**
   * 为角色添加权限
   */
  @PostMapping("/roles/{roleName}/permissions")
  fun addPermissionToRole(
    @PathVariable roleName: String,
    @RequestParam resource: String,
    @RequestParam action: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.addPolicy(roleName, resource, action)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "roleName" to roleName,
      "resource" to resource,
      "action" to action,
      "message" to if (success) "权限添加成功" else "权限添加失败"
    ))
  }

  /**
   * 从角色移除权限
   */
  @DeleteMapping("/roles/{roleName}/permissions")
  fun removePermissionFromRole(
    @PathVariable roleName: String,
    @RequestParam resource: String,
    @RequestParam action: String
  ): ResponseEntity<Map<String, Any>> {
    val success = permissionService.removePolicy(roleName, resource, action)
    return ResponseEntity.ok(mapOf(
      "success" to success,
      "roleName" to roleName,
      "resource" to resource,
      "action" to action,
      "message" to if (success) "权限移除成功" else "权限移除失败"
    ))
  }

  /**
   * 获取角色权限列表
   */
  @GetMapping("/roles/{roleName}/permissions")
  fun getRolePermissions(@PathVariable roleName: String): ResponseEntity<Map<String, Any>> {
    val permissions = permissionService.getRolePermissions(roleName)
    return ResponseEntity.ok(mapOf(
      "roleName" to roleName,
      "permissions" to permissions,
      "count" to permissions.size
    ))
  }
}
