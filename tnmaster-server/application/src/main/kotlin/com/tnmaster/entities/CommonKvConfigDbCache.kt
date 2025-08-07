package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key

/**
 * # 数据库缓存
 *
 * @author TrueNine
 * @since 2025-03-01
 */
@Entity
interface CommonKvConfigDbCache : IEntity {
  /** 存储 key */
  @Key
  val k: String

  /** 存储 value */
  val v: String?
}
