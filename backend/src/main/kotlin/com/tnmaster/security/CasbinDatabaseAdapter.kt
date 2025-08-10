package com.tnmaster.security

import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import com.tnmaster.repositories.IUserAccountRepo
import io.github.truenine.composeserver.slf4j
import org.casbin.jcasbin.persist.Adapter
import org.casbin.jcasbin.model.Model
import org.springframework.stereotype.Component

/**
 * # Casbin 数据库适配器
 * 
 * 从现有的权限数据库表中加载权限策略
 *
 * @author TrueNine
 * @since 2025-01-10
 */
@Component
class CasbinDatabaseAdapter(
  private val userAccountRepo: IUserAccountRepo,
  private val roleGroupRepo: IRoleGroupRepo,
  private val roleRepo: IRoleRepo
) : Adapter {
  
  companion object {
    private val log = slf4j<CasbinDatabaseAdapter>()
  }

  /**
   * 从数据库加载所有策略
   */
  override fun loadPolicy(model: Model) {
    log.info("开始从数据库加载 Casbin 策略")
    
    try {
      // 加载用户-角色组关系 (g)
      loadUserRoleGroupPolicies(model)
      
      // 加载角色组-角色关系 (g2)
      loadRoleGroupRolePolicies(model)
      
      // 加载角色-权限关系 (p)
      loadRolePermissionPolicies(model)
      
      log.info("Casbin 策略加载完成")
    } catch (e: Exception) {
      log.error("加载 Casbin 策略失败", e)
      throw e
    }
  }

  /**
   * 保存策略到数据库（暂不实现，使用现有的权限管理接口）
   */
  override fun savePolicy(model: Model) {
    log.warn("savePolicy 方法未实现，请使用现有的权限管理接口进行权限变更")
  }

  /**
   * 添加策略（暂不实现）
   */
  override fun addPolicy(sec: String, ptype: String, rule: List<String>) {
    log.warn("addPolicy 方法未实现")
  }

  /**
   * 移除策略（暂不实现）
   */
  override fun removePolicy(sec: String, ptype: String, rule: List<String>) {
    log.warn("removePolicy 方法未实现")
  }

  /**
   * 移除过滤的策略（暂不实现）
   */
  override fun removeFilteredPolicy(sec: String, ptype: String, fieldIndex: Int, vararg fieldValues: String) {
    log.warn("removeFilteredPolicy 方法未实现")
  }

  /**
   * 加载用户-角色组关系
   */
  private fun loadUserRoleGroupPolicies(model: Model) {
    try {
      // 查询所有用户及其角色组
      val userRoleGroups = userAccountRepo.findAllUserRoleGroups()
      
      userRoleGroups.forEach { (account, roleGroupName) ->
        // g, user, roleGroup
        model.addPolicy("g", "g", listOf(account, roleGroupName))
      }
      
      log.info("加载了 {} 条用户-角色组关系", userRoleGroups.size)
    } catch (e: Exception) {
      log.error("加载用户-角色组关系失败", e)
    }
  }

  /**
   * 加载角色组-角色关系
   */
  private fun loadRoleGroupRolePolicies(model: Model) {
    try {
      // 查询所有角色组及其角色
      val roleGroupRoles = roleGroupRepo.findAllRoleGroupRoles()
      
      roleGroupRoles.forEach { (roleGroupName, roleName) ->
        // g2, roleGroup, role
        model.addPolicy("g", "g2", listOf(roleGroupName, roleName))
      }
      
      log.info("加载了 {} 条角色组-角色关系", roleGroupRoles.size)
    } catch (e: Exception) {
      log.error("加载角色组-角色关系失败", e)
    }
  }

  /**
   * 加载角色-权限关系
   */
  private fun loadRolePermissionPolicies(model: Model) {
    try {
      // 查询所有角色及其权限
      val rolePermissions = roleRepo.findAllRolePermissions()
      
      rolePermissions.forEach { (roleName, permissionName) ->
        // p, role, permission, allow
        model.addPolicy("p", "p", listOf(roleName, permissionName, "allow"))
      }
      
      log.info("加载了 {} 条角色-权限关系", rolePermissions.size)
    } catch (e: Exception) {
      log.error("加载角色-权限关系失败", e)
    }
  }
}
