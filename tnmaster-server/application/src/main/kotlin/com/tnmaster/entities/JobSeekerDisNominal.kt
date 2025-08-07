package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.OneToOne

/** 残疾就职特殊简历 */
@Entity
interface JobSeekerDisNominal : IEntity {
  /** 所属简历 */
  @OneToOne
  @JoinColumn(name = "job_seeker_id")
  val jobSeeker: JobSeeker

  @IdView("jobSeeker")
  val jobSeekerId: RefId

  /** 接受证件抵押 */
  val accCollateralCert: Boolean?

  /** 接受押金 */
  val accCollateralMoney: Boolean?

  /** 可以去线下面签 */
  val accOfflineInterview: Boolean?

  /** 现有医保 */
  val hasMedicalSupport: Boolean?

  /** 拥有社保 */
  val hasSocialSupport: Boolean?

  /** 拥有低保 */
  val hasLowIncomeSupport: Boolean?
}
