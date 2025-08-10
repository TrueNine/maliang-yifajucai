package com.tnmaster.repositories

import com.tnmaster.entities.Role
import com.tnmaster.entities.fetchBy
import com.tnmaster.entities.name
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IRoleRepo : IRepo<Role, RefId> {
  fun findAllRoleNames(): Set<String> {
    return sql.createQuery(Role::class) { select(table.name) }.execute().toSet()
  }

  /**
   * 查询所有角色-权限关系，用于 Casbin 策略加载
   */
  fun findAllRolePermissions(): List<Pair<String, String>> {
    return sql
      .createQuery(Role::class) {
        select(table.fetchBy { 
          name()
          permissions { name() } 
        })
      }
      .execute()
      .flatMap { role ->
        role.permissions.map { permission ->
          role.name to permission.name
        }
      }
  }
}
