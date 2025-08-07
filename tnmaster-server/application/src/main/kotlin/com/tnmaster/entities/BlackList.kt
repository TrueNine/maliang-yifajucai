package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.date
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.AuditTyping
import io.github.truenine.composeserver.rds.typing.RelationItemTyping
import org.babyfish.jimmer.sql.*

@Entity
interface BlackList : IEntity {

  /** 黑名单标签 */
  @ManyToMany
  val blackListTags: List<BlackListTag>

  /** 审核状态 */
  val auditStatus: AuditTyping?

  @IdView("createUserAccount")
  val createUserId: RefId?

  /** 创建此黑名单的用户 */
  @ManyToOne
  @JoinColumn(name = "create_user_id")
  val createUserAccount: UserAccount?

  @IdView("reportUserAccount")
  val reportUserId: RefId?

  /** 报告者账号 */
  @ManyToOne
  @JoinColumn(name = "report_user_id")
  @OnDissociate(DissociateAction.SET_NULL)
  val reportUserAccount: UserAccount?

  @IdView("reportUserInfo")
  val reportUserInfoId: RefId?

  /** 报告者的用户信息 */
  @ManyToOne
  @JoinColumn(name = "report_user_info_id")
  val reportUserInfo: UserInfo?

  /** 被拉黑的账号 */
  @Deprecated("不建议使用")
  @IdView("blackRefUserAccount")
  val blackRefId: RefId?

  @IdView("blackUserInfo")
  val blackUserInfoId: RefId?

  /** 被拉黑的用户信息 */
  @ManyToOne
  @JoinColumn(name = "black_user_info_id")
  val blackUserInfo: UserInfo?

  /** 被拉黑的账号 */
  @Deprecated("不建议使用")
  @ManyToOne
  @JoinColumn(name = "black_ref_id")
  @OnDissociate(DissociateAction.SET_NULL)
  val blackRefUserAccount: UserAccount?

  val reItemType: RelationItemTyping?
  val createDatetime: datetime?

  /** 事件描述 */
  val eventDoc: String

  /** 发生日期 */
  val onDate: date?

  /** 黑名单所有证据 */
  @OneToMany(mappedBy = "blackList")
  val evidences: List<BlackListEvidence>

  /** 黑名单事件的关系 */
  @OneToMany(mappedBy = "blackList")
  val blackListRelations: List<BlackListRelation>
}
