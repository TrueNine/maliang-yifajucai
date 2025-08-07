package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.typing.HTTPMethod
import org.babyfish.jimmer.sql.*

@Entity
interface Api : IEntity {
  val name: String?
  val doc: String?

  @Key
  val apiPath: String?

  @Key
  val apiMethod: HTTPMethod?

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
