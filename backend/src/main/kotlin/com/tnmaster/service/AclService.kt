package com.tnmaster.service

import com.tnmaster.dto.api.ApiPermitVariantView
import com.tnmaster.repositories.IApiRepo
import com.tnmaster.repositories.IPermissionsRepo
import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import com.tnmaster.repositories.IUserAccountRepo
import io.github.truenine.composeserver.consts.ICacheNames
import io.github.truenine.composeserver.slf4j
import org.casbin.jcasbin.main.Enforcer
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * # 访问控制管理 API
 *
 * @author TrueNine
 * @since 2025-03-01
 */
@Service
class AclService(
  private val apiRepo: IApiRepo,
  private val roleGroupRepo: IRoleGroupRepo,
  private val roleRepo: IRoleRepo,
  private val permissionsRepo: IPermissionsRepo,
  private val userAccountRepo: IUserAccountRepo,
  private val enforcer: Enforcer
) {
  companion object {
    private val log = slf4j<AclService>()
  }

  /**
   * ## 所有需校验的 API
   * - 拥有权限 id
   * - 需要登录
   */
  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::api::all'")
  fun fetchAllPermitVariantApis(): List<ApiPermitVariantView> {
    return apiRepo.findALlPermissionsVariant()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::permissions::names::all'")
  fun fetchAllPermissionsNames(): Set<String> {
    return permissionsRepo.findAllPermissionsNames()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::role::names::all'")
  fun fetchAllRoleNames(): Set<String> {
    return roleRepo.findAllRoleNames()
  }

  @Cacheable(ICacheNames.M5, cacheManager = ICacheNames.IRedis.CACHE_MANAGER, key = "'acl::roleGroup::names::all'")
  fun fetchAllRoleGroupNames(): Set<String> {
    return roleGroupRepo.findAllRoleGroupNames()
  }

  /**
   * 检查用户是否拥有指定权限
   */
  fun hasPermission(account: String, permission: String): Boolean {
    return enforcer.enforce(account, permission, "allow")
  }

  /**
   * 检查用户是否拥有指定角色
   */
  fun hasRole(account: String, role: String): Boolean {
    return enforcer.hasRoleForUser(account, role)
  }

  /**
   * 获取用户的所有角色
   */
  fun getUserRoles(account: String): List<String> {
    return enforcer.getRolesForUser(account)
  }

  /**
   * 获取用户的所有权限
   */
  fun getUserPermissions(account: String): Set<String> {
    return userAccountRepo.findAllPermissionsNameByAccount(account)
  }

  /**
   * 重新加载权限策略
   */
  fun reloadPolicy() {
    try {
      enforcer.loadPolicy()
      log.info("权限策略重新加载成功")
    } catch (e: Exception) {
      log.error("权限策略重新加载失败", e)
      throw e
    }
  }

  /**
   * 添加用户角色关系
   */
  fun addRoleForUser(account: String, role: String): Boolean {
    return try {
      val result = enforcer.addRoleForUser(account, role)
      if (result) {
        log.info("为用户 {} 添加角色 {} 成功", account, role)
      }
      result
    } catch (e: Exception) {
      log.error("为用户 {} 添加角色 {} 失败", account, role, e)
      false
    }
  }

  /**
   * 移除用户角色关系
   */
  fun removeRoleForUser(account: String, role: String): Boolean {
    return try {
      val result = enforcer.deleteRoleForUser(account, role)
      if (result) {
        log.info("为用户 {} 移除角色 {} 成功", account, role)
      }
      result
    } catch (e: Exception) {
      log.error("为用户 {} 移除角色 {} 失败", account, role, e)
      false
    }
  }

  /**
   * 添加权限策略
   */
  fun addPermissionForRole(role: String, permission: String): Boolean {
    return try {
      val result = enforcer.addPermissionForUser(role, permission, "allow")
      if (result) {
        log.info("为角色 {} 添加权限 {} 成功", role, permission)
      }
      result
    } catch (e: Exception) {
      log.error("为角色 {} 添加权限 {} 失败", role, permission, e)
      false
    }
  }

  /**
   * 移除权限策略
   */
  fun removePermissionForRole(role: String, permission: String): Boolean {
    return try {
      val result = enforcer.deletePermissionForUser(role, permission, "allow")
      if (result) {
        log.info("为角色 {} 移除权限 {} 成功", role, permission)
      }
      result
    } catch (e: Exception) {
      log.error("为角色 {} 移除权限 {} 失败", role, permission, e)
      false
    }
  }
}
