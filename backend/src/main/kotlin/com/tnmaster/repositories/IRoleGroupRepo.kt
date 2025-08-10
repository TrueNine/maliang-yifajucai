package com.tnmaster.repositories

import com.tnmaster.entities.RoleGroup
import com.tnmaster.entities.fetchBy
import com.tnmaster.entities.name
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IRoleGroupRepo : IRepo<RoleGroup, RefId> {
  fun findAllRoleGroupNames(): Set<String> {
    return sql.createQuery(RoleGroup::class) { select(table.name) }.execute().toSet()
  }

  /**
   * 查询所有角色组-角色关系，用于 Casbin 策略加载
   */
  fun findAllRoleGroupRoles(): List<Pair<String, String>> {
    return sql
      .createQuery(RoleGroup::class) {
        select(table.fetchBy { 
          name()
          roles { name() } 
        })
      }
      .execute()
      .flatMap { roleGroup ->
        roleGroup.roles.map { role ->
          roleGroup.name to role.name
        }
      }
  }
}
