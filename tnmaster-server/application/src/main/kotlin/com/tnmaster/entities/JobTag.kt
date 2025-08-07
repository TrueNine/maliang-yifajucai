package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity

/** 职位标签，用于筛选 */
@Entity
interface JobTag : IEntity {
  val name: String
  val doc: String?
}
