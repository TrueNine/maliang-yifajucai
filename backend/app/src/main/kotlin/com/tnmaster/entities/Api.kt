package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.enums.HttpMethod
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate

@Entity
interface Api : IEntity {
  val name: String?
  val doc: String?

  @Key
  val apiPath: String?

  @Key
  val apiMethod: HttpMethod?

  val apiProtocol: String?

  @IdView("permissions")
  val permissionsId: RefId?

  /** 所属权限 */
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  @JoinColumn(name = "permissions_id")
  val permissions: Permissions?

  /** 该接口是否需要登录 */
  val requireLogin: Boolean?
}
