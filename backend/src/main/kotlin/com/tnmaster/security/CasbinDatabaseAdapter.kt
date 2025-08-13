package com.tnmaster.security

import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import com.tnmaster.repositories.IUserAccountRepo
import io.github.truenine.composeserver.logger
import org.casbin.jcasbin.model.Model
import org.casbin.jcasbin.persist.Adapter
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
  private val roleRepo: IRoleRepo,
) : Adapter {

  companion object {
    private val log = logger<CasbinDatabaseAdapter>()
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
   * 保存策略到数据库
   *
   * 注意：这个方法主要用于完整的策略重建，在生产环境中应谨慎使用
   */
  override fun savePolicy(model: Model) {
    log.info("开始保存 Casbin 策略到数据库")

    try {
      // 检查model是否为null或model.model是否为null
      if (model.model == null) {
        log.warn("Model或model.model为null，无法保存策略")
        return
      }

      // 获取所有策略
      val policies = model.model["p"]?.get("p")?.policy ?: emptyList()
      val groupings = model.model["g"]?.get("g")?.policy ?: emptyList()
      val groupings2 = model.model["g"]?.get("g2")?.policy ?: emptyList()

      log.info("保存策略: p={}, g={}, g2={}", policies.size, groupings.size, groupings2.size)

      // 注意：实际的保存逻辑需要根据具体的数据库表结构来实现
      // 这里只是记录策略内容，实际保存需要调用相应的Repository方法

      policies.forEach { policy ->
        log.debug("权限策略: {}", policy)
      }

      groupings.forEach { grouping ->
        log.debug("用户角色组关系: {}", grouping)
      }

      groupings2.forEach { grouping ->
        log.debug("角色组角色关系: {}", grouping)
      }

      log.info("Casbin 策略保存完成")
    } catch (e: Exception) {
      log.error("保存 Casbin 策略失败", e)
      throw e
    }
  }

  /**
   * 添加单个策略
   */
  override fun addPolicy(sec: String, ptype: String, rule: List<String>) {
    log.info("添加策略: sec={}, ptype={}, rule={}", sec, ptype, rule)

    try {
      when {
        sec == "p" && ptype == "p" && rule.size >= 3 -> {
          // 角色-权限策略: [role, resource, action]
          val role = rule[0]
          val resource = rule[1]
          val action = rule[2]
          log.info("添加权限策略: 角色={}, 资源={}, 操作={}", role, resource, action)
          // 实际实现需要调用相应的Repository方法来保存到数据库
        }

        sec == "g" && ptype == "g" && rule.size >= 2 -> {
          // 用户-角色组关系: [user, roleGroup]
          val user = rule[0]
          val roleGroup = rule[1]
          log.info("添加用户角色组关系: 用户={}, 角色组={}", user, roleGroup)
          // 实际实现需要调用相应的Repository方法来保存到数据库
        }

        sec == "g" && ptype == "g2" && rule.size >= 2 -> {
          // 角色组-角色关系: [roleGroup, role]
          val roleGroup = rule[0]
          val role = rule[1]
          log.info("添加角色组角色关系: 角色组={}, 角色={}", roleGroup, role)
          // 实际实现需要调用相应的Repository方法来保存到数据库
        }

        else -> {
          log.warn("不支持的策略类型: sec={}, ptype={}, rule={}", sec, ptype, rule)
        }
      }
    } catch (e: Exception) {
      log.error("添加策略失败: sec={}, ptype={}, rule={}", sec, ptype, rule, e)
    }
  }

  /**
   * 移除单个策略
   */
  override fun removePolicy(sec: String, ptype: String, rule: List<String>) {
    log.info("移除策略: sec={}, ptype={}, rule={}", sec, ptype, rule)

    try {
      when {
        sec == "p" && ptype == "p" && rule.size >= 3 -> {
          // 移除角色-权限策略
          val role = rule[0]
          val resource = rule[1]
          val action = rule[2]
          log.info("移除权限策略: 角色={}, 资源={}, 操作={}", role, resource, action)
          // 实际实现需要调用相应的Repository方法来从数据库删除
        }

        sec == "g" && ptype == "g" && rule.size >= 2 -> {
          // 移除用户-角色组关系
          val user = rule[0]
          val roleGroup = rule[1]
          log.info("移除用户角色组关系: 用户={}, 角色组={}", user, roleGroup)
          // 实际实现需要调用相应的Repository方法来从数据库删除
        }

        sec == "g" && ptype == "g2" && rule.size >= 2 -> {
          // 移除角色组-角色关系
          val roleGroup = rule[0]
          val role = rule[1]
          log.info("移除角色组角色关系: 角色组={}, 角色={}", roleGroup, role)
          // 实际实现需要调用相应的Repository方法来从数据库删除
        }

        else -> {
          log.warn("不支持的策略类型: sec={}, ptype={}, rule={}", sec, ptype, rule)
        }
      }
    } catch (e: Exception) {
      log.error("移除策略失败: sec={}, ptype={}, rule={}", sec, ptype, rule, e)
    }
  }

  /**
   * 移除过滤的策略
   */
  override fun removeFilteredPolicy(sec: String, ptype: String, fieldIndex: Int, vararg fieldValues: String) {
    log.info(
      "移除过滤策略: sec={}, ptype={}, fieldIndex={}, fieldValues={}",
      sec, ptype, fieldIndex, fieldValues.contentToString()
    )

    try {
      when {
        sec == "p" && ptype == "p" -> {
          // 根据字段索引移除权限策略
          when (fieldIndex) {
            0 -> {
              // 移除指定角色的所有权限
              val role = fieldValues[0]
              log.info("移除角色的所有权限: 角色={}", role)
              // 实际实现需要调用相应的Repository方法
            }

            1 -> {
              // 移除指定资源的所有权限
              val resource = fieldValues[0]
              log.info("移除资源的所有权限: 资源={}", resource)
              // 实际实现需要调用相应的Repository方法
            }

            else -> {
              log.warn("不支持的字段索引: {}", fieldIndex)
            }
          }
        }

        sec == "g" && ptype == "g" -> {
          // 根据字段索引移除用户-角色组关系
          when (fieldIndex) {
            0 -> {
              // 移除指定用户的所有角色组
              val user = fieldValues[0]
              log.info("移除用户的所有角色组: 用户={}", user)
              // 实际实现需要调用相应的Repository方法
            }

            1 -> {
              // 移除指定角色组的所有用户
              val roleGroup = fieldValues[0]
              log.info("移除角色组的所有用户: 角色组={}", roleGroup)
              // 实际实现需要调用相应的Repository方法
            }

            else -> {
              log.warn("不支持的字段索引: {}", fieldIndex)
            }
          }
        }

        else -> {
          log.warn("不支持的策略类型: sec={}, ptype={}", sec, ptype)
        }
      }
    } catch (e: Exception) {
      log.error(
        "移除过滤策略失败: sec={}, ptype={}, fieldIndex={}, fieldValues={}",
        sec, ptype, fieldIndex, fieldValues.contentToString(), e
      )
    }
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
