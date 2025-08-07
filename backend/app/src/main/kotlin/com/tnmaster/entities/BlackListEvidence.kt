package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.AuditTyping
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne

/** # 黑名单证据 */
@Entity
interface BlackListEvidence : IEntity {
  @IdView("attachment")
  val attId: RefId

  /** 黑名单附件 */
  @ManyToOne
  @JoinColumn(name = "att_id")
  val attachment: Attachment

  @IdView("blackList")
  val blackListId: RefId

  /** 所属黑名单 */
  @ManyToOne
  val blackList: BlackList

  /** 证据描述 */
  val doc: String?

  /** 证据标题 */
  val title: String?

  /** 审核状态 */
  val auditStatus: AuditTyping?
}
