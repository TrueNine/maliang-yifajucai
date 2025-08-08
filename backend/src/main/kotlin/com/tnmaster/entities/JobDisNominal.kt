package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.decimal
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.OneToOne
import java.time.Period

/** 残疾特殊职位 */
@Entity
interface JobDisNominal : IEntity {
  /** 每月任务次数 */
  val taskOnMonthCount: Int?

  /** 不接受拥有医保，包括新农合 */
  val notMedicalSupport: Boolean?

  /** 不接受拥有社保 */
  val notSocialSupport: Boolean?

  /** 不接受拥有低保 */
  val notLowIncomeSupport: Boolean?

  /** 允许紧急情况撤出合同 */
  val exceptionOut: Boolean?

  /** 后续是否可续签合同 */
  val hasRenewContract: Boolean?

  /** 面签补贴费用 */
  val offlineInterviewAmount: decimal?

  /** 是否需要面签 */
  val hasOfflineInterview: Boolean?

  /** 特殊合同期限 */
  val contractPeriod: Period?

  /** 每年任务次数 */
  val taskOnYearCount: Int?

  @IdView("job")
  val jobId: RefId

  /** 所属职位 */
  @OneToOne
  val job: Job
}
