package com.tnmaster.entities

import com.tnmaster.entities.resolvers.EnterpriseAddressResolver
import com.tnmaster.enums.EnterpriseType
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.date
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.decimal
import io.github.truenine.composeserver.enums.ISO4217
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Transient

/**
 * 企业
 */
@Entity
interface Enterprise : IEntity {
  @Deprecated("直接绑定账号即可")
  @ManyToOne
  @JoinColumn(name = "leader_user_info_id")
  val leaderUserInfo: UserInfo?

  @IdView("leaderUserInfo")
  val leaderUserInfoId: RefId?

  /**
   * 该企业所绑定的账号
   */
  @ManyToOne
  @JoinColumn(name = "leader_user_id")
  val leaderUserAccount: UserAccount?

  @IdView("leaderUserAccount")
  val leaderUserAccountId: RefId?

  /**
   * 数据创建时间
   */
  val createDatetime: datetime?

  @ManyToOne
  @JoinColumn(name = "address_details_id")
  val addressDetails: AddressDetails?

  @IdView("addressDetails")
  val addressDetailsId: RefId?

  val addressCode: String?

  val enterpriseType: EnterpriseType?

  @ManyToOne
  val industry: Industry?

  @IdView("industry")
  val industryId: RefId?

  @ManyToOne
  val logoAttachment: Attachment?

  @IdView("logoAttachment")
  val logoAttachmentId: RefId?

  @ManyToOne
  @JoinColumn(name = "rpi")
  val parentEnterprise: Enterprise?

  @OneToMany(mappedBy = "parentEnterprise")
  val childrenEnterprises: List<Enterprise>

  @IdView("parentEnterprise")
  val rpi: RefId?

  val title: String?
  val employeeCount: Long?
  val doc: String?
  val leaderName: String?
  val regCapital: decimal?
  val capitalCurrency: ISO4217?
  val regDate: date?
  val status: Int?

  @Transient(EnterpriseAddressResolver::class)
  val address: Address?

  /**
   * 统一社会信用代码
   */
  val creditCodeV1: String?

  /**
   * 数据评分
   */
  val dataScore: Int?

  /**
   * 所有发布的职位
   */
  @OneToMany(mappedBy = "enterprise")
  val publishedJobs: List<Job>
}
