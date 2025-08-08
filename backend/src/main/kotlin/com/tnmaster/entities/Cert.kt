package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.enums.CertContentTyping
import io.github.truenine.composeserver.rds.enums.CertPointTyping
import io.github.truenine.composeserver.rds.enums.CertTyping
import org.babyfish.jimmer.sql.Default
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate

/** 用户证件 */
@Entity
interface Cert : IEntity {
  val name: String?
  val doc: String?
  val remark: String?

  val createDatetime: datetime?
  val createIp: String?
  val createDeviceId: String?

  @IdView("createUserAccount")
  val createUserId: RefId?

  /** 创建此证件的账号 */
  @ManyToOne
  @JoinColumn(name = "create_user_Id")
  val createUserAccount: UserAccount?

  /** 审核状态 */
  val auditStatus: AuditTyping?

  @IdView("metaAttachment")
  val attId: RefId

  /** 原始证件 */
  @ManyToOne
  @JoinColumn(name = "att_id")
  @OnDissociate(DissociateAction.DELETE)
  val metaAttachment: Attachment

  @IdView("waterMarkerAttachment")
  val wmAttId: RefId?

  /** 水印图片 */
  @ManyToOne
  @JoinColumn(name = "wm_att_id")
  @OnDissociate(DissociateAction.DELETE)
  val waterMarkerAttachment: Attachment?

  @IdView("userInfo")
  val userInfoId: RefId?

  /** 所属用户信息 */
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  val userInfo: UserInfo?

  /** 所属账号 */
  @ManyToOne
  @JoinColumn(name = "user_id")
  @OnDissociate(DissociateAction.SET_NULL)
  val userAccount: UserAccount?

  @IdView("userAccount")
  val userId: RefId?

  /** 证件印面类型 */
  val poType: CertPointTyping?

  /** 证件内容类型 */
  val coType: CertContentTyping?

  /** 证件类型 */
  val doType: CertTyping?

  /**
   * 水印码
   *
   * water marker code
   */
  @Key
  val wmCode: String?

  /** 证件组编号 */
  @Key
  val groupCode: String?

  /** 对于用户是否可见 */
  @Default("true")
  val visible: Boolean?
}
