package com.tnmaster.repositories

import com.tnmaster.entities.DisInfo
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.account
import com.tnmaster.entities.by
import com.tnmaster.entities.certCode
import com.tnmaster.entities.disInfo
import com.tnmaster.entities.email
import com.tnmaster.entities.id
import com.tnmaster.entities.idCard
import com.tnmaster.entities.phone
import com.tnmaster.entities.pri
import com.tnmaster.entities.userAccountId
import com.tnmaster.entities.wechatOpenid
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IDisCode
import io.github.truenine.composeserver.domain.IIdcard2Code
import io.github.truenine.composeserver.rds.IRepo
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.DisTyping
import io.github.truenine.composeserver.rds.enums.GenderTyping
import io.github.truenine.composeserver.rds.jimmerextpostgres.substr
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.expression.`valueIn?`
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.babyfish.jimmer.sql.kt.exists
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

/** 用户信息 */
@Primary
@Repository
interface IUserInfoRepo : IRepo<UserInfo, RefId> {
  fun existsByEmail(
    email: String? = null,
  ): Boolean {
    return sql.createQuery(
      UserInfo::class
    ) {
      where(
        table.email eq email
      )
      select(table.id)
    }.exists()
  }

  /**
   * ## 根据用户账户ID保存身份证信息
   *
   * 该函数负责根据用户账户ID和提供的身份证号码，生成并保存用户信息，包括生日、性别等 它首先验证身份证号码的有效性，然后根据身份证号码获取相关信息，并更新用户信息
   *
   * @param userAccountId 用户账户的唯一标识符，用于查找和关联用户信息
   * @param idCard 身份证号码，必须是有效且真实的18位号码
   * @param base 基础信息，用于更新用户信息
   * @return 保存后的用户信息列表，通常包含一个用户信息对象
   * @throws IllegalArgumentException 如果身份证号码为空或长度不是18位
   */
  @ACID
  fun saveIdCardByUserAccountId(userAccountId: RefId, idCard: String, base: UserInfo = UserInfo {}): List<UserInfo> {
    require(idCard.isNotBlank()) { "idCard is blank" }
    require(idCard.length == 18) { "idCard length is not 18" }
    val idCardInfo = IIdcard2Code[idCard]
    val userInfos =
      findAllByUserAccountIds(listOf(userAccountId)).map { userInfo ->
        UserInfo(base = base) {
          account = UserAccount { id = userAccountId }
          id = userInfo.id
          addressCode = idCardInfo.idcardDistrictCode
          birthday = idCardInfo.idcardBirthday
          gender = if (idCardInfo.idcardSex) GenderTyping.MAN else GenderTyping.WOMAN
          this.idCard = idCardInfo.idcard2Code
        }
      }
    require(userInfos.isNotEmpty()) { "userInfo is empty" }
    return saveAll(userInfos)
  }

  /**
   * ## 保存用户账户的残疾信息。
   *
   * @param userAccountId 用户账户的唯一标识符，类型为 [RefId]。
   * @param disInfo 残疾信息
   * @param base 用户信息的基础数据，默认值为空的 [UserInfo] 对象。
   * @return 返回一个包含更新后的 [UserInfo] 对象的列表。
   *
   * 该函数的主要逻辑包括：
   * 1. 将输入的残疾信息转换为实体对象。
   * 2. 根据用户账户 ID 查询相关用户信息，并验证残疾证件号码的有效性。
   * 3. 更新用户信息中的身份证、生日、性别以及残疾信息。
   * 4. 保存更新后的用户信息并返回结果列表。
   */
  @ACID
  fun saveDisInfoByUserAccountId(userAccountId: RefId, disInfo: DisInfo, base: UserInfo = UserInfo {}): List<UserInfo> {
    val userInfos =
      findAllByUserAccountIds(listOf(userAccountId)).map { userInfo ->
        UserInfo(base = base) {
          account = UserAccount { id = userAccountId }
          userInfo.idCard?.also { idCard -> require(disInfo.certCode?.startsWith(idCard) == true) { "disInfo.certCode is not start with idCard" } }
          val c = IDisCode[disInfo.certCode!!]
          id = userInfo.id
          idCard = c.idcard2Code
          addressCode = c.idcardDistrictCode
          birthday = c.idcardBirthday
          gender = if (c.idcardSex) GenderTyping.MAN else GenderTyping.WOMAN
          this.disInfo =
            DisInfo(base = disInfo) {
              certCode = c.disCode
              dsType = DisTyping[c.disType]!!
              level = c.disLevel
            }
        }
      }
    require(userInfos.isNotEmpty()) { "userInfo is empty" }
    return saveAll(userInfos)
  }

  /** ## 根据 idCard 查询所有用户信息 */
  fun findAllByUserAccountIdAndIdCard(userAccountId: RefId, idCardCode: String, fetcher: Fetcher<UserInfo>? = null): List<UserInfo> {
    return sql
      .createQuery(UserInfo::class) {
        where(table.account.id eq userAccountId, table.idCard eq idCardCode, table.idCard.isNotNull())
        select(table.fetch(fetcher))
      }
      .execute()
  }

  /**
   * ## 根据用户账户ID和身份证代码查询残疾信息列表
   *
   * 该方法通过用户账户ID和身份证代码来查询相关的残疾信息它使用SQL查询构建器来构造查询条件， 包括用户账户ID匹配、身份证代码的前18位匹配，并确保残疾信息的证件代码不为空
   *
   * @param userAccountId 用户账户ID，用于定位特定用户
   * @param idCardCode 身份证代码，用于匹配残疾信息中的证件代码
   * @param fetcher 可选的Fetcher对象，用于指定获取数据的策略
   * @return 匹配条件的残疾信息列表
   */
  fun findDisInfosByUserAccountIdAndIdCardCode(userAccountId: RefId, idCardCode: String, fetcher: Fetcher<DisInfo>? = null): List<DisInfo> {
    return sql
      .createQuery(UserInfo::class) {
        where(table.account.id eq userAccountId, table.disInfo.certCode.substr(1, 18) eq idCardCode, table.disInfo.certCode.isNotNull())
        select(table.disInfo.fetch(fetcher))
      }
      .execute()
  }

  /** ## 根据账号 id 查询所有用户信息 id */
  fun findIdsByUserAccountId(userAccountId: RefId): List<RefId> {
    return sql
      .createQuery(UserInfo::class) {
        where(table.userAccountId eq userAccountId)
        select(table.id)
      }
      .execute()
  }

  fun existsByDisInfoCertCode(disInfoCertCode: String): Boolean {
    return sql.exists(UserInfo::class) { where(table.disInfo.certCode eq disInfoCertCode) }
  }

  fun existsByIdCard(idCard: String): Boolean {
    return sql.exists(UserInfo::class) { where(table.idCard eq idCard) }
  }

  fun existsByPhone(phone: String): Boolean {
    return sql.exists(UserInfo::class) { where(table.phone eq phone) }
  }

  /**
   * ## 根据账号 查询首个用户信息
   *
   * @param account 账号
   * @param fetcher 抓取器
   */
  fun findFirstByUserAccount(account: String, fetcher: Fetcher<UserInfo>): UserInfo? {
    return sql
      .createQuery(UserInfo::class) {
        where(table.account.account eq account)
        where(table.pri eq true)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .execute()
      .firstOrNull()
  }

  /**
   * ## 查询 微信 open id 是否存在
   *
   * @param wechatOpenId 微信 open id
   */
  fun existsByWechatOpenId(wechatOpenId: String) = sql.exists(UserInfo::class) { where(table.wechatOpenid eq wechatOpenId) }

  fun findFirstByWechatOpenId(wechatOpenId: String, fetcher: Fetcher<UserInfo>? = null): UserInfo? {
    return sql
      .createQuery(UserInfo::class) {
        where(table.wechatOpenid eq wechatOpenId)
        where(table.pri eq true)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }

  fun findFirstIdByUserAccountId(
    userAccountId: RefId,
    isPrimary: Boolean? = null,
  ): RefId? {
    return sql
      .createQuery(UserInfo::class) {
        where(
          table.account.id eq userAccountId,
          table.pri `eq?` isPrimary
        )

        select(
          table.id
        )
      }
      .limit(1)
      .offset(0)
      .execute()
      .firstOrNull()
  }

  /**
   * ## 根据用户账户ID查找第一条残疾信息
   * > 通过用户账户ID查询关联的第一条残疾信息，支持自定义字段抓取器
   *
   * 该函数通过用户账户ID在数据库中查询关联的第一条残疾信息，并支持通过 `fetcher` 参数自定义需要抓取的字段。
   *
   * @param userAccountId 用户账户ID，类型为 `RefId`
   * @param fetcher 可选参数，用于指定需要抓取的字段，类型为 `Fetcher<DisInfo>?`，默认为 `null`
   * @return 返回查询到的第一条残疾信息，类型为 `DisInfo?`，如果未找到则返回 `null`
   */
  fun findFirstDisInfoByUserAccountId(userAccountId: RefId, fetcher: Fetcher<DisInfo>? = null): DisInfo? {
    return sql
      .createQuery(UserInfo::class) {
        where(table.account.id eq userAccountId)
        select(table.disInfo.fetch(fetcher))
      }
      .execute()
      .firstOrNull()
  }

  /**
   * ## 根据查询条件查找第一个用户信息
   * > 根据指定的查询条件和字段选择器，查找第一个匹配的用户信息。
   *
   * 详细描述：
   * 1. 使用 `KSpecification` 构建查询条件。
   * 2. 使用 `Fetcher` 选择需要查询的字段。
   * 3. 查询结果按主键排序，并限制返回结果为第一条。
   *
   * @param spec 查询条件，使用 `KSpecification` 构建。
   * @param fetcher 字段选择器，默认为选择所有标量字段。
   * @return 返回第一个匹配的用户信息，如果没有匹配项则返回 `null`。
   */
  fun findFirstBySpec(spec: KSpecification<UserInfo>, fetcher: Fetcher<UserInfo>? = newFetcher(UserInfo::class).by { allScalarFields() }): UserInfo? {
    return sql
      .createQuery(UserInfo::class) {
        where(spec)
        orderBy(table.pri)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }

  /** 根据userId查询，如果存在多个，则返回第一个 */
  fun findFirstByUserAccountIdOrNull(userAccountId: RefId, fetcher: Fetcher<UserInfo>? = null): UserInfo? {
    return sql
      .createQuery(UserInfo::class) {
        where(table.userAccountId eq userAccountId)
        orderBy(table.pri)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .execute()
      .firstOrNull()
  }

  fun findAllByUserAccountIds(userAccountIds: List<RefId>, fetcher: Fetcher<UserInfo>? = null): List<UserInfo> {
    if (userAccountIds.isEmpty()) {
      return emptyList()
    }
    return sql
      .createQuery(UserInfo::class) {
        where(table.userAccountId `valueIn?` userAccountIds)
        orderBy(table.pri)
        select(table.fetch(fetcher))
      }
      .execute()
  }

  /**
   * ## 保存用户账户ID关联的电话号码，如果电话号码已存在则不进行保存
   *
   * 此方法旨在确保每个用户账户ID至多关联一个电话号码如果传入的电话号码为空或只包含空白字符，则不执行任何操作 使用ACID注解确保操作的原子性、一致性、隔离性和持久性
   *
   * @param userAccountId 用户账户ID，用于标识用户账户的唯一键
   * @param phone 可能要保存的电话号码，如果为空或空白则不进行保存
   */
  @ACID
  fun saveNotExistsPhoneByUserAccountId(userAccountId: RefId, phone: String?) {
    if (phone.isNullOrBlank()) {
      return
    }
    val exists = sql.exists(UserInfo::class) { where(table.userAccountId eq userAccountId, table.phone.isNull()) }
    if (exists) {
      sql.createUpdate(UserInfo::class) {
        where(table.userAccountId eq userAccountId, table.phone.isNull())
        set(table.phone, phone)
      }
    }
  }
}
