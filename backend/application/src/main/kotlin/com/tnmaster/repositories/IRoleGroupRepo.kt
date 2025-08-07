package com.tnmaster.repositories

import com.tnmaster.entities.RoleGroup
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
}
