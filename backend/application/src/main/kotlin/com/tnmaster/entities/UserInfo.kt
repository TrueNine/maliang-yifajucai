package com.tnmaster.entities

import com.tnmaster.entities.converter.BlankStringSensitiveJsonConverter
import com.tnmaster.entities.converter.IdCardSensitiveJsonConverter
import com.tnmaster.entities.converter.NameSensitiveJsonConverter
import com.tnmaster.entities.converter.PhoneSensitiveJsonConverter
import com.tnmaster.entities.resolvers.UserInfoAddressResolver
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.date
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.enums.GenderTyping
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OnDissociate
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Transient

/** # 自然人用户信息 */
@Entity
interface UserInfo : IEntity {
  @JsonConverter(BlankStringSensitiveJsonConverter::class)
  @Key(group = "soft")
  val firstName: String?

  @Formula(dependencies = ["firstName"])
  val metadataFirstName: String?
    get() = firstName

  @JsonConverter(BlankStringSensitiveJsonConverter::class)
  @Key(group = "soft")
  val lastName: String?

  @Formula(dependencies = ["lastName"])
  val metadataLastName: String?
    get() = lastName

  /** 全名 */
  @Formula(sql = "concat(%alias.first_name, %alias.last_name)")
  @JsonConverter(NameSensitiveJsonConverter::class)
  val name: String?

  @Formula(sql = "concat(%alias.first_name, %alias.last_name)")
  val metadataName: String?

  /** 出生日期 */
  val birthday: date?

  @Formula(sql = "extract(year from age(current_date,%alias.birthday))")
  val age: Int?

  /** 用户所关联的账号 */
  @IdView("account")
  val userAccountId: RefId?

  /** 用户账号主体 */
  @Key
  @OneToOne
  @OnDissociate(DissociateAction.SET_NULL)
  @JoinColumn(name = "user_id")
  val account: UserAccount?

  /** 主信息数据库字段 */
  val pri: Boolean?

  /** 是否为主信息 */
  @Formula(dependencies = ["pri"])
  val primaryUserInfo: Boolean
    get() = pri == true

  /** 创建此信息的账号 id */
  @IdView("createUserAccount")
  val createUserId: RefId?

  /** 创建此信息的账号 */
  @Key(group = "hard")
  @ManyToOne
  @OnDissociate(DissociateAction.SET_NULL)
  @JoinColumn(name = "create_user_id")
  val createUserAccount: UserAccount?

  @Key(group = "hard")
  val email: String?

  /** 用户手机号 */
  @Key(group = "hard")
  @JsonConverter(PhoneSensitiveJsonConverter::class)
  val phone: String?

  @Formula(dependencies = ["phone"])
  val metadataPhone: String?
    get() = phone

  /** 备用手机号码（非紧急联系人） */
  @Key(group = "soft")
  @JsonConverter(PhoneSensitiveJsonConverter::class)
  val sparePhone: String?

  @Formula(dependencies = ["sparePhone"])
  val metadataSparePhone: String?
    get() = sparePhone

  @Key(group = "hard")
  @JsonConverter(IdCardSensitiveJsonConverter::class)
  val idCard: String?

  @Formula(dependencies = ["idCard"])
  val metadataIdCard: String?
    get() = idCard

  /** 性别 */
  val gender: GenderTyping?

  @Key(group = "hard")
  val wechatOpenid: String?

  @Deprecated("该字段不固定，微信账号会发生变化")
  val wechatAccount: String?

  @Deprecated("暂时废弃")
  val wechatAuthid: String?

  @Deprecated("暂不使用")
  val qqOpenid: String?

  @Key(group = "hard")
  val qqAccount: String?

  /** 地理位置编码 */
  @Key(group = "hard")
  val addressCode: String?

  /** 所关联地址 */
  @Transient(UserInfoAddressResolver::class)
  val address: Address?

  /** 此用户备注 */
  val remark: String?

  /** 企业内备注名称 */
  @Key(group = "soft")
  @JsonConverter(NameSensitiveJsonConverter::class)
  val remarkName: String?

  @Formula(dependencies = ["remarkName"])
  val metadataRemarkName: String?
    get() = remarkName

  /** 残疾信息 */
  @OneToOne(mappedBy = "userInfo")
  val disInfo: DisInfo?

  /** 该用户所属的简历 */
  @OneToOne(mappedBy = "userInfo")
  val jobSeeker: JobSeeker?

  /** 黑名单记录 */
  @OneToMany(mappedBy = "blackUserInfo")
  val blackListRecords: List<BlackList>

  /** 该用户是否被拉黑 */
  @Formula(sql = "exists(select 1 from black_list where black_list.black_user_info_id = %alias.id)")
  val isBlacked: Boolean

  /** 被拉黑次数 */
  @Formula(dependencies = ["blackListRecords"])
  val blackedSize: Int
    get() = blackListRecords.size

  /** 该用户信息的所有证件 */
  @OneToMany(mappedBy = "userInfo")
  val certs: List<Cert>

  /** 充当的黑名单关系 */
  @OneToMany(mappedBy = "userInfo")
  val blackListRelations: List<BlackListRelation>

  /**
   * 被标记的管理的企业
   */
  @OneToMany(mappedBy = "leaderUserInfo")
  val markManagedEnterprises: List<Enterprise>
}
