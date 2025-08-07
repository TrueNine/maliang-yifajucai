package com.tnmaster.entities.embeddable

import io.github.truenine.composeserver.decimal
import org.babyfish.jimmer.sql.Embeddable

/** 所接受的工资类型 */
@Embeddable
interface ExSalaryRange {
  /** 接受的最大工资 */
  val exMaxSalary: decimal?

  /** 接受的最低工资 */
  val exMinSalary: decimal?
}
