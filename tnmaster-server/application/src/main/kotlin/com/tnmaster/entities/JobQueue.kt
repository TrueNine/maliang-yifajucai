package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne

/** 职位入职队列 */
@Entity
interface JobQueue : IEntity {
  /** 排序权重 */
  val orderWeight: Long

  /** 取消，撤离时间 */
  val cancelDatetime: datetime?

  /** 是否已被取消 */
  @Formula(dependencies = ["cancelDatetime"])
  val canceled: Boolean
    get() = cancelDatetime != null

  /** 创建时间 */
  val createDatetime: datetime

  @IdView("userInfo")
  val userInfoId: RefId?

  /** 应聘用户信息 */
  @ManyToOne
  @JoinColumn(name = "user_info_id")
  val userInfo: UserInfo?

  @IdView("userAccount")
  val userId: RefId?

  /** 应聘用户 */
  @ManyToOne
  @JoinColumn(name = "user_id")
  val userAccount: UserAccount?

  @IdView("jobSeeker")
  val jobSeekerId: RefId

  /** 所属简历 */
  @ManyToOne
  val jobSeeker: JobSeeker

  @IdView("job")
  val jobId: RefId

  /** 所属职位 */
  @ManyToOne
  val job: Job
}
