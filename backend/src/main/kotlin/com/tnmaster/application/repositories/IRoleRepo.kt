package com.tnmaster.application.repositories

import com.tnmaster.entities.Role
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
}
