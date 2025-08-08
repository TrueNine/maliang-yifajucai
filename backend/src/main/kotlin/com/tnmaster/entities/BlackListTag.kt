package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key

/** # 黑名单标签 */
@Entity
interface BlackListTag : IEntity {
  @Key
  val name: String
  val doc: String?
}
