package com.tnmaster.entities

import com.tnmaster.entities.converter.PhoneSensitiveJsonConverter
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.*

/** 个人银行卡
 * @param region 我的
 * */
@Entity
interface BankCard : IEntity {
  @Default("true")
  val available: Boolean

  /**
   * 于个人的使用强度排序权重
   */
  @Default("0")
  val orderWeight: Int?

  /**
   * 结算区域，以表示国家
   * @see io.github.truenine.composeserver.typing.ISO4217 遵照此标准
   */
  val region: String?

  /**
   * 银行全名，禁止自身创建
   */
  val bankName: String?

  /**
   * 银行组织
   */
  val bankGroupName: String?

  @ManyToOne
  val userAccount: UserAccount

  @IdView("userAccount")
  val userAccountId: RefId

  @Key
  val code: String?

  /** 所属证件 */
  @ManyToMany
  val certs: List<Cert>

  @Default("true")
  val visible: Boolean?

  /** 联系手机 */
  @JsonConverter(PhoneSensitiveJsonConverter::class)
  val phone: String?

  /** 开户行地址 */
  val issueLocation: String?
}
