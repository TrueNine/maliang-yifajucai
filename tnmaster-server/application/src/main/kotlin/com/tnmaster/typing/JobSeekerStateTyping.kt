package com.tnmaster.typing

import com.fasterxml.jackson.annotation.JsonValue
import io.github.truenine.composeserver.IIntTyping
import org.babyfish.jimmer.sql.EnumItem
import org.babyfish.jimmer.sql.EnumType

/** 求职者状态 */
@EnumType(EnumType.Strategy.ORDINAL)
enum class JobSeekerStateTyping(v: Int) : IIntTyping {
  /**
   * 无状态
   *
   * 等同于空
   */
  @EnumItem(ordinal = 0)
  NONE(0),

  /** 等待入职 */
  @EnumItem(ordinal = 1)
  WAITING(1),

  /** 已入职 */
  @EnumItem(ordinal = 2)
  SUCCESS(2),

  /**
   * 已撤离
   *
   * 例如：去了别的平台等
   */
  @EnumItem(ordinal = 3)
  LEAVE(3),

  /**
   * 不可用
   *
   * 例如：被拉黑，被劝退
   */
  @EnumItem(ordinal = 4)
  UNAVAILABLE(4),

  /**
   * 其他状态
   *
   * 其他未来扩展状态
   */
  @EnumItem(ordinal = 9999)
  OTHER(9999);

  @JsonValue
  override val value: Int = v

  companion object {
    @JvmStatic
    operator fun get(v: Int?): JobSeekerStateTyping? = entries.find { it.value == v }
  }
}
