package com.tnmaster.application.enums

import io.github.truenine.composeserver.IIntTyping
import org.babyfish.jimmer.sql.EnumItem
import org.babyfish.jimmer.sql.EnumType

/**
 * 企业类别
 */
@EnumType(EnumType.Strategy.ORDINAL)
enum class EnterpriseType(
  override val value: Int,
) : IIntTyping {
  @EnumItem(ordinal = 0)
  NONE(0),

  /**
   * 中介
   */
  @EnumItem(ordinal = 1)
  MIDDLEMAN(1),

  /**
   * 企业
   */
  @EnumItem(ordinal = 2)
  ENTERPRISE(2),

  /**
   * 其他
   */
  @EnumItem(ordinal = 9999)
  OTHER(9999);

  companion object {
    @JvmStatic
    operator fun get(v: Int?): EnterpriseType? {
      return entries.find { it.value == v }
    }
  }
}
