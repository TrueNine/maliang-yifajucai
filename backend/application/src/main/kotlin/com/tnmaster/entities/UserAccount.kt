package com.tnmaster.entities

import com.tnmaster.entities.converter.BlankStringSensitiveJsonConverter
import com.tnmaster.entities.converter.SerialNoSensitiveJsonConverter
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.rds.entities.IEntity
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.OneToOne

/**
 * # 用户账号
 *
 * @author TrueNine
 * @since 2024-12-09
 */
@Entity
interface UserAccount : IEntity {
  /** 用户账号 */
  @Key(group = "account")
  @JsonConverter(SerialNoSensitiveJsonConverter::class)
  val account: String

  @Formula(dependencies = ["account"])
  val metadataAccount: String
    get() = account

  /** 加密后的密码 */
  @JsonConverter(BlankStringSensitiveJsonConverter::class)
  val pwdEnc: String

  @Formula(dependencies = ["pwdEnc"])
  val metadataPwdEnc: String
    get() = pwdEnc

  /** 该用户所属的用户信息 */
  @Key
  @OneToOne(mappedBy = "account")
  val userInfo: UserInfo?

  @ManyToOne
  @JoinColumn(name = "create_user_id")
  val createUserAccount: UserAccount?
  val nickName: String?
  val doc: String?

  val banTime: datetime?

  /** 是否已被禁用 */
  @Formula(dependencies = ["banTime"])
  val disabled: Boolean
    get() = banTime?.let { t -> t > datetime.now() } == true

  val lastLoginTime: datetime?

  @IdView("createUserAccount")
  val createUserId: RefId?

  /** 用户拥有的角色组 */
  @ManyToMany
  @JoinTable(name = "user_role_group", joinColumnName = "user_id")
  val roleGroups: List<RoleGroup>

  /** 该账号的入职状态 */
  @OneToOne(mappedBy = "userAccount")
  val jobSeekerStatus: JobSeekerStatus?

  /** 充当的黑名单关系人 */
  @OneToMany(mappedBy = "refUserAccount")
  val blackListRelations: List<BlackListRelation>

  /** 账号对应的证件 */
  @OneToMany(mappedBy = "userAccount")
  val certs: List<Cert>

  /**
   * 该账号所管理的企业
   */
  @OneToMany(mappedBy = "leaderUserAccount")
  val managedEnterprises: List<Enterprise>
}
