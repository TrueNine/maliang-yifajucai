package com.tnmaster.entities

import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany

@Entity
interface RoleGroup : IEntity {
  @Key
  val name: String
  val doc: String?

  /** 拥有该角色组的用户账号 */
  @ManyToMany(mappedBy = "roleGroups")
  val userAccounts: List<UserAccount>

  /** 该角色组其下的角色 */
  @ManyToMany
  val roles: List<Role>
}
