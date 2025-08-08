package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.AuditTyping
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne

/** 简历附带的个税视频？ */
@Entity
interface JobSeekerDisNominalTaxVideo : IEntity {
  /** 审核状态 */
  val auditStatus: AuditTyping?

  @ManyToOne
  @JoinColumn(name = "job_seeker_id")
  val jobSeeker: JobSeeker

  @IdView("jobSeeker")
  val jobSeekerId: RefId

  @IdView("attachment")
  val attId: RefId

  @IdView("auditAccount")
  val auditUserId: RefId?

  /** 审核账号 */
  @ManyToOne
  @JoinColumn(name = "audit_user_id")
  val auditAccount: UserAccount?

  @IdView("complaintAuditAccount")
  val complaintAuditUserId: RefId?

  /** 申诉审核账号 */
  @ManyToOne
  @JoinColumn(name = "complaint_audit_user_id")
  val complaintAuditAccount: UserAccount?

  /** 审核时间 */
  val auditDatetime: datetime

  /** 申诉时间 */
  val complaintAuditDatetime: datetime

  /** 申诉审核状态 */
  val complaintAuditStatus: AuditTyping?

  /** 申诉描述 */
  val complaint: String?

  /** 出现问题的描述 */
  val problemDesc: String?

  /** 是否存在问题 */
  val problem: Boolean?

  /** 个税视频查询时间 */
  val queryDatetime: datetime

  /** 附带的个税视频 */
  @ManyToOne
  @JoinColumn(name = "att_id")
  val attachment: Attachment
}
