package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.PlatformType
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany

/**
 * # 菜单
 *
 * @author TrueNine
 * @since 2025-02-26
 */
@Entity
interface Menu : IEntity {
  /** 平台类型 */
  @Key(group = "uni")
  val platformType: PlatformType

  /** 菜单 */
  val title: String

  /** 匹配路径 */
  @Key(group = "uni")
  val pattern: String

  /** 是否需要登录 */
  val requireLogin: Boolean

  /** 描述 */
  val doc: String?

  /** 所需角色 */
  @ManyToMany
  val roles: List<Role>

  /** 所需权限 */
  @ManyToMany
  val permissions: List<Permissions>
}
