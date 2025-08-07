package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key

@Entity
interface Permissions : IEntity {
  @Key
  val name: String
  val doc: String?
}
