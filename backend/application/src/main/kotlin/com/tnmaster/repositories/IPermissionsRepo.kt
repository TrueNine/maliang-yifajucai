package com.tnmaster.repositories

import com.tnmaster.entities.Permissions
import com.tnmaster.entities.name
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IPermissionsRepo : IRepo<Permissions, RefId> {
  fun findAllPermissionsNames(): Set<String> {
    return sql.createQuery(Permissions::class) { select(table.name) }.execute().toSet()
  }
}
