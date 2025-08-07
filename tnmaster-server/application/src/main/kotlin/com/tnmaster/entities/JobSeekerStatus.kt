package com.tnmaster.entities

import com.tnmaster.typing.JobSeekerStateTyping
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.decimal
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.AuditTyping
import org.babyfish.jimmer.sql.*

/** 简历的入职状态 */
@Entity
interface JobSeekerStatus : IEntity {
  val jobId: RefId?

  @IdView("userAccount")
  val userAccountId: RefId

  /** 所属账号 */
  @OneToOne
  @JoinColumn(name = "user_id")
  val userAccount: UserAccount

  @IdView("jobSeeker")
  val jobSeekerId: RefId

  /** 所属简历 */
  @OneToOne
  val jobSeeker: JobSeeker

  @IdView("auditUserAccount")
  val auditUserId: RefId?

  /** 入职状态 */
  val status: JobSeekerStateTyping?

  /** 创建时间 */
  val createdDatetime: datetime

  /** 审核状态 */
  val auditStatus: AuditTyping?

  /** 审核人账号 */
  @ManyToOne
  @JoinColumn(name = "audit_user_id")
  val auditUserAccount: UserAccount?

  /** 金额 */
  val amount: decimal?

  /** 备注 */
  val remark: String?
}
