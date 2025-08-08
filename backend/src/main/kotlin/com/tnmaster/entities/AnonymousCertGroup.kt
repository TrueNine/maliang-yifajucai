package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate

/** # 匿名证件组 */
@Entity
interface AnonymousCertGroup : IEntity {
  /** 上传证件 id */
  @IdView("cert")
  val certId: RefId

  /** 上传证件 */
  @Key
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  @JoinColumn(name = "cert_id")
  val cert: Cert

  /** 上传账号 id */
  @IdView("uploadUserAccount")
  val upUserId: RefId

  /** 上传账号 */
  @Key
  @ManyToOne
  @JoinColumn(name = "up_user_id")
  val uploadUserAccount: UserAccount

  /** 证件序列号 */
  @Key
  val serialNo: String
}
