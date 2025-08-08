package com.tnmaster.entities

import com.tnmaster.entities.converter.PeriodToStringConverter
import com.tnmaster.entities.embeddable.StartEndDateRange
import com.tnmaster.enums.EnterpriseType
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.decimal
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.enums.DegreeTyping
import io.github.truenine.composeserver.rds.enums.GenderTyping
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne
import java.time.Period

/** 职位 */
@Entity
interface Job : IEntity {
  /** 正在等待此职位的 等待队列 */
  @OneToMany(mappedBy = "job")
  val waitingQueues: List<JobQueue>

  /** 标题 */
  val title: String

  /** 职位标签 */
  @ManyToMany
  @JoinTable(name = "job_job_tag", joinColumnName = "job_id", inverseJoinColumnName = "job_tag_id")
  val jobTags: List<JobTag>

  /** 简短描述 */
  val doc: String?

  /** 所属特殊简历信息 */
  @OneToOne(mappedBy = "job")
  val jobDisNominal: JobDisNominal?

  /** 岗位职责 */
  val postResp: String?

  /** 任职要求 */
  val qualification: String?

  /** 生命周期，持续周期，在列表内多久下架 */
  val lifecycle: StartEndDateRange

  /**
   * 发布职位的企业
   */
  @ManyToOne
  val enterprise: Enterprise?

  @IdView("enterprise")
  val enterpriseId: RefId?

  @IdView("addressDetails")
  val addressDetailsId: RefId?

  /** 所需填写的特殊表单 */
  @Deprecated("暂不使用")
  val rqContractFormGroupId: RefId?

  /** 是否为私有职位 */
  val privated: Boolean?

  /** 职位准备状态 */
  @Deprecated("暂不使用")
  val readyStatus: Int?

  /** 奖金补贴规则 */
  val subsidy: String?

  /**
   * 薪资规则
   */
  val salaryCommissionRule: String?

  /**
   * 每月薪水发放时间
   */
  val payday: Int?

  /**
   * 创建时间
   */
  val createDatetime: datetime

  /**
   * 工作所在地
   */
  val addressCode: String?

  /**
   * 限制员工所在地
   */
  val rqAddressCode: String?

  /**
   * 工作地所在地址
   */
  @ManyToOne
  @JoinColumn(name = "address_details_id")
  val addressDetails: AddressDetails?

  /**
   * 联系座机
   */
  val landline: String?

  /**
   * 联系电话
   */
  val phone: String?

  /**
   * 联系人姓名
   */
  val contactName: String?

  /**
   * 最小工资
   */
  val minSalary: decimal?

  /**
   * 最大工资
   */
  val maxSalary: decimal?

  val maxManAge: Int?
  val minManAge: Int?
  val maxWomanAge: Int?
  val minWomanAge: Int?

  /**
   * 所需行业的经验年限
   */
  val exYear: Int?

  /**
   * 所需学历
   */
  val degree: DegreeTyping?

  /**
   * 职位类型
   */
  @Deprecated("暂时不用")
  val jobType: Int?

  /** 从事周期 */
  @JsonConverter(PeriodToStringConverter::class)
  val postPeriod: Period?

  /**
   * 所需残疾类别
   */
  val rqDisRule: ByteArray?

  /**
   * 岗位类型
   */
  @Deprecated("暂不使用")
  val postType: Int?

  @IdView("enterpriseUserAccount")
  @Deprecated("暂不使用")
  val enterpriseUserId: RefId?

  /** 发布者企业账号 */
  @ManyToOne
  @JoinColumn(name = "enterprise_user_id")
  @Deprecated("暂不使用")
  val enterpriseUserAccount: UserAccount?

  /** 企业类型 */
  @Deprecated("暂不使用")
  val enterpriseType: EnterpriseType?

  /** 派遣类型 */
  @Deprecated("暂不使用")
  val dispatchType: Int?

  @IdView("industry")
  val industryId: RefId?

  /** 所属行业 */
  @ManyToOne
  val industry: Industry?

  /** 排序权重 */
  val orderedWeight: Int

  @IdView("userInfo")
  val userInfoId: RefId?

  /** 发布者用户信息 */
  @ManyToOne
  val userInfo: UserInfo?

  /** 性别限制 */
  val rqGender: GenderTyping?

  /** 所需人数 */
  val rqCount: Int

  /** 审核状态 */
  val auditStatus: AuditTyping?

  /** 职位详情图片 */
  @ManyToMany
  @JoinTable(name = "job_details_img", joinColumnName = "job_id", inverseJoinColumnName = "img_att_id")
  val detailsImages: List<Attachment>
}
