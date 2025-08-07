package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.typing.ISO4217
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key

/** ## 银行类型 */
@Entity
interface Bank : IEntity {
  @Key
  val groupType: String

  @Key
  val bankName: String

  @Key
  val region: ISO4217
}
