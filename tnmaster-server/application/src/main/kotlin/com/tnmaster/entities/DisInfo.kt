package com.tnmaster.entities

import com.tnmaster.entities.converter.DisCardSensitiveJsonConverter
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.DisTyping
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.jackson.JsonConverter
import org.babyfish.jimmer.sql.*

/** 残疾证信息 */
@Entity
interface DisInfo : IEntity {
  /** 残疾的具体部位 */
  val place: ByteArray?

  /** 造成残疾的原因 */
  val cause: String?

  /** 残疾证号 */
  @Key
  @JsonConverter(DisCardSensitiveJsonConverter::class)
  val certCode: String?

  @Formula(dependencies = ["certCode"])
  val metadataCertCode: String?
    get() = certCode

  /** 等级 */
  val level: Int?

  /** 残疾类别 */
  val dsType: DisTyping?

  /** 所属用户信息 */
  @Key
  @OneToOne
  @OnDissociate(DissociateAction.SET_NULL)
  @JoinColumn(name = "user_info_id")
  val userInfo: UserInfo?

  @IdView("userInfo")
  val userInfoId: RefId?
}
