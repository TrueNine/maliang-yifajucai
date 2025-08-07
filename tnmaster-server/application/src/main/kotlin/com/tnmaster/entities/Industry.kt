package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key

/** 行业 */
@Entity
interface Industry : IEntity {
  /** 行业名称 */
  @Key
  val title: String

  /** 行业描述 */
  val doc: String?
}
