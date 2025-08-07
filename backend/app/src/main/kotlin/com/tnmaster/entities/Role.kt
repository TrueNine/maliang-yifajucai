package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany

@Entity
interface Role : IEntity {
  @Key
  val name: String
  val doc: String?

  @ManyToMany
  val permissions: List<Permissions>
}
