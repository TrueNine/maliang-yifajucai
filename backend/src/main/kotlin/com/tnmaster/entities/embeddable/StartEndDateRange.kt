package com.tnmaster.entities.embeddable

import io.github.truenine.composeserver.date
import org.babyfish.jimmer.sql.Embeddable

/** 开始和结束天数 */
@Embeddable
interface StartEndDateRange {
  val startDate: date?
  val endDate: date?
}
