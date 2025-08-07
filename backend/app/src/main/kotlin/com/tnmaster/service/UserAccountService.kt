package com.tnmaster.service

import com.tnmaster.dto.cert.CertAdminCertifiedUserInfoSpec
import com.tnmaster.dto.userinfo.UserInfoAdminCertifiedView
import com.tnmaster.dto.userinfo.UserInfoAdminPostDto
import com.tnmaster.dto.userinfo.UserInfoAdminPutDto
import com.tnmaster.dto.userinfo.UserInfoPutDto
import com.tnmaster.entities.BankCard
import com.tnmaster.entities.Cert
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.auditStatus
import com.tnmaster.entities.bankName
import com.tnmaster.entities.by
import com.tnmaster.entities.crd
import com.tnmaster.entities.doType
import com.tnmaster.entities.id
import com.tnmaster.entities.idCard
import com.tnmaster.entities.phone
import com.tnmaster.entities.userAccountId
import com.tnmaster.entities.userId
import com.tnmaster.entities.userInfoId
import com.tnmaster.entities.visible
import com.tnmaster.repositories.ICertRepo
import com.tnmaster.repositories.IUserAccountRepo
import com.tnmaster.repositories.IUserInfoRepo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.logger
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.enums.CertTyping
import io.github.truenine.composeserver.rds.fetchPq
import io.github.truenine.composeserver.rds.toFetcher
import io.github.truenine.composeserver.toId
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.exists
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.ast.expression.notExists
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.ast.expression.`valueIn?`
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service

@Service
class UserAccountService(
  private val userAccountRepo: IUserAccountRepo, private val certRepo: ICertRepo, private val userInfoRepo: IUserInfoRepo,
) {
  companion object {
    @JvmStatic
    private val log = logger<UserAccountService>()
  }

  /**
   * ## 根据用户账户ID获取用户信息是否通过公民认证
   *
   * 该函数用于检查指定用户账户ID对应的用户信息是否已经通过公民认证。认证条件包括：
   * - 用户信息中必须包含有效的手机号和身份证号
   * - 用户信息必须与指定的用户账户ID匹配
   * - 不存在与用户账户ID或用户信息ID关联的未通过审核的证件信息
   *
   * @param userAccountId 用户账户ID，用于查询关联的用户信息
   * @return 如果用户信息通过公民认证则返回`true`，否则返回`false`
   */
  fun fetchUserInfoIsCitizenVerifiedByUserAccountId(
    userAccountId: RefId,
  ): Boolean {
    log.trace("[fetchUserInfoIsCitizenVerifiedByUserAccountId] called with userAccountId={}", userAccountId)
    val userInfoId = userInfoRepo.findFirstIdByUserAccountId(userAccountId)
    log.trace("[fetchUserInfoIsCitizenVerifiedByUserAccountId] resolved userInfoId={}", userInfoId)
    val result = userInfoRepo.sql.createQuery(UserInfo::class) {
      val certsCheck = subQuery(Cert::class) {
        where(
          or(
            table.userId eq userAccountId, table.userInfoId eq userInfoId
          ), table.visible eq true, or(
            table.auditStatus ne AuditTyping.PASS
          )
        )
        select(table.id)
      }
      where(
        table.phone.isNotNull(), table.idCard.isNotNull(), table.id eq userInfoId, table.userAccountId eq userAccountId, notExists(certsCheck)
      )
      select(table.id)
    }.exists()
    log.trace("[fetchUserInfoIsCitizenVerifiedByUserAccountId] result={}", result)
    return result
  }

  /**
   * ## 根据用户账户ID获取用户信息认证视图
   * > 通过指定的查询参数获取用户信息的认证视图
   *
   * 该函数根据传入的查询参数，从认证仓库中查找已认证的用户信息，并将其转换为管理员认证视图。
   *
   * @param spec 查询参数，用于指定查找用户信息的条件
   * @return 返回用户信息的管理员认证视图，如果未找到则返回null
   */
  fun fetchUserInfoCertifyViewByUserAccountId(spec: CertAdminCertifiedUserInfoSpec): UserInfoAdminCertifiedView? {
    log.trace("[fetchUserInfoCertifyViewByUserAccountId] called with spec={}", spec)
    val result =
      certRepo.findCertifiedUserInfoBySpec(spec = spec, fetcher = UserInfoAdminCertifiedView::class.toFetcher())?.let { UserInfoAdminCertifiedView(it) }
    log.trace("[fetchUserInfoCertifyViewByUserAccountId] result={}", result)
    return result
  }

  /**
   * ## 根据用户账号 ID 获取用户信息 DTO
   * > 此方法通过用户账号 ID 查询并返回用户信息 DTO。
   * - 使用 `userInfoRepo.findFirstByUserAccountIdOrNull` 方法查询用户信息。
   * - 使用 `UserInfoPutDto::class.toFetcher()` 构造查询 fetcher。
   * - 如果查询结果不为空，则将其转换为 `UserInfoPutDto` 对象。
   *
   * @param userAccountId 用户账号主键 ID，用于定位用户相关信息。
   * @return 返回一个 `UserInfo` 对象，如果未找到则返回 `null`。
   */
  fun fetchPutDtoByUserAccountId(userAccountId: RefId): UserInfo? {
    log.trace("[fetchPutDtoByUserAccountId] called with userAccountId={}", userAccountId)
    val result = userInfoRepo.findFirstByUserAccountIdOrNull(userAccountId, fetcher = UserInfoPutDto::class.toFetcher())?.also { UserInfoPutDto(it) }
    log.trace("[fetchPutDtoByUserAccountId] result={}", result)
    return result
  }

  /**
   * ## 创建或更新用户信息（管理员）
   * > 包含事务操作，支持唯一性校验
   *
   * 1. 将传输对象转换为实体时：
   *    - 强制绑定创建者ID
   *    - 标记为非隐私用户
   * 2. 使用UPSERT模式保存（存在则更新，不存在则插入）
   * 3. 关联实体采用全量替换策略
   * 4. 设置复合唯一键约束：
   *    - `idCard` 证件号码
   *    - `phone` 手机号
   *    - 约束级别为强校验（hard）
   *
   * @param createUserAccountId 创建者用户ID（需已存在的账户）
   * @param dto 包含用户基本信息的传输对象
   * @return 持久化后的用户信息实体（包含数据库生成字段）
   */
  @ACID
  fun postCustomerUserInfo(
    createUserAccountId: RefId,
    dto: UserInfoAdminPostDto,
  ): UserInfo {
    log.trace("[postCustomerUserInfo] called with createUserAccountId={}, dto={}", createUserAccountId, dto)
    val existingUser = userInfoRepo.findFirstByUserAccountIdOrNull(
      createUserAccountId, newFetcher(UserInfo::class).by {
        allScalarFields()
        name()
      })
    log.trace("[postCustomerUserInfo] existingUser={}", existingUser)
    if (dto.idCard != null) {
      val existingIdCard = userInfoRepo.sql.createQuery(UserInfo::class) {
        where(table.idCard eq dto.idCard)
        if (existingUser != null) {
          where(table.id ne existingUser.id)
        }
        where(table.userAccountId ne createUserAccountId)
        select(table.id)
      }.exists()
      log.trace("[postCustomerUserInfo] existingIdCard check result={}", existingIdCard)
      if (existingIdCard) {
        log.trace("[postCustomerUserInfo] throw: idCard already exists: {}", dto.idCard)
        throw IllegalArgumentException("身份证号 ${dto.idCard} 已存在，不能重复使用")
      }
    }
    if (dto.phone != null) {
      val existingPhone = userInfoRepo.sql.createQuery(UserInfo::class) {
        where(table.phone eq dto.phone)
        if (existingUser != null) {
          where(table.id ne existingUser.id)
        }
        where(table.userAccountId ne createUserAccountId)
        select(table.id)
      }.exists()
      log.trace("[postCustomerUserInfo] existingPhone check result={}", existingPhone)
      if (existingPhone) {
        log.trace("[postCustomerUserInfo] throw: phone already exists: {}", dto.phone)
        throw IllegalArgumentException("手机号 ${dto.phone} 已存在，不能重复使用")
      }
    }
    val result = userInfoRepo.saveCommand(
      dto.toEntity {
        if (existingUser != null) {
          this.id = existingUser.id
        }
        this.createUserId = createUserAccountId
        this.pri = false
      }, SaveMode.UPSERT, AssociatedSaveMode.REPLACE
    ) {
      setKeyProps(
        "hard", UserInfo::idCard, UserInfo::phone
      )
    }.execute(newFetcher(UserInfo::class).by {
      allScalarFields()
      name()
      createUserId()
    }).modifiedEntity
    log.trace("[postCustomerUserInfo] result={}", result)
    return result
  }

  @ACID
  fun modifyCustomerUserInfo(
    createUserAccountId: RefId,
    dto: UserInfoAdminPutDto,
  ): UserInfo {
    log.trace("[modifyCustomerUserInfo] called with createUserAccountId={}, dto={}", createUserAccountId, dto)
    if (dto.idCard != null) {
      val existingIdCard = userInfoRepo.sql.createQuery(UserInfo::class) {
        where(table.idCard eq dto.idCard)
        where(table.id.isNotNull())
        where(table.id ne dto.id.toId())
        where(table.userAccountId.isNotNull())
        where(table.userAccountId ne createUserAccountId)
        select(table.id)
      }.exists()
      log.trace("[modifyCustomerUserInfo] existingIdCard check result={}", existingIdCard)
      if (existingIdCard) {
        log.trace("[modifyCustomerUserInfo] throw: idCard already exists: {}", dto.idCard)
        throw IllegalArgumentException("身份证号 ${dto.idCard} 已存在，不能重复使用")
      }
    }
    if (dto.phone != null) {
      val existingPhone = userInfoRepo.sql.createQuery(UserInfo::class) {
        where(table.phone eq dto.phone)
        where(table.id.isNotNull())
        where(table.id ne dto.id.toId())
        where(table.userAccountId.isNotNull())
        where(table.userAccountId ne createUserAccountId)
        select(table.id)
      }.exists()
      log.trace("[modifyCustomerUserInfo] existingPhone check result={}", existingPhone)
      if (existingPhone) {
        log.trace("[modifyCustomerUserInfo] throw: phone already exists: {}", dto.phone)
        throw IllegalArgumentException("手机号 ${dto.phone} 已存在，不能重复使用")
      }
    }
    val result = userInfoRepo.saveCommand(
      dto.toEntity {
        this.createUserId = createUserAccountId
        this.pri = false
      }, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE
    ) {
      setKeyProps(
        "hard", UserInfo::idCard, UserInfo::phone
      )
    }.execute(UserInfoAdminPutDto::class.toFetcher()).modifiedEntity
    log.trace("[modifyCustomerUserInfo] result={}", result)
    return result
  }

  /**
   * ## 检测残疾证号是否包含身份证号
   * - 如果本身不具备身份证号，返回 `true`
   * - 如果残疾证号包含身份证号，返回 `false`
   * - 否则，抛出异常
   *
   * @param disInfoCertCode 残疾证号
   */
  fun checkDisInfoCertCodeContainsIdCardOrThrow(userAccountId: RefId, disInfoCertCode: String) {
    log.trace("[checkDisInfoCertCodeContainsIdCardOrThrow] called with userAccountId={}, disInfoCertCode={}", userAccountId, disInfoCertCode)
    require(disInfoCertCode.isNotBlank()) { "disInfoCertCode is blank" }
    require(disInfoCertCode.length == 20 || disInfoCertCode.length == 22) { "disInfoCertCode length must be 20 or 22" }
    val allIdCards =
      userInfoRepo.findAllByUserAccountIdAndIdCard(userAccountId, disInfoCertCode.substring(0, 18), fetcher = newFetcher(UserInfo::class).by { idCard() })
    log.trace("[checkDisInfoCertCodeContainsIdCardOrThrow] allIdCards={}", allIdCards)
    if (allIdCards.isEmpty()) {
      log.trace("[checkDisInfoCertCodeContainsIdCardOrThrow] no idCard found, return")
      return
    } else {
      val allIn = allIdCards.all { disInfoCertCode.startsWith(it.idCard!!) }
      log.trace("[checkDisInfoCertCodeContainsIdCardOrThrow] allIn={}", allIn)
      require(allIn) { "disInfoCertCode contains idCard" }
    }
  }

  /**
   * ## 检查身份证号是否存在于残疾证号中
   * > 验证指定用户的身份证号是否符合以下规则：
   * - 如果不存在与该用户关联的残疾信息，则直接返回。
   * - 如果存在残疾信息，则进一步验证所有残疾证号是否以该身份证号开头。
   * - 若不符合上述规则，则抛出异常。
   *
   * @param userAccountId 用户账号主键 ID，用于定位用户相关信息。
   * @param idCardCertCode 身份证号，长度必须为 18 位且不能为空，用于匹配残疾证号。
   */
  fun checkIdCardCertCodeInDisCertCodeOrThrow(userAccountId: RefId, idCardCertCode: String) {
    log.trace("[checkIdCardCertCodeInDisCertCodeOrThrow] called with userAccountId={}, idCardCertCode={}", userAccountId, idCardCertCode)
    require(idCardCertCode.isNotBlank()) { "idCardCertCode is blank" }
    require(idCardCertCode.length == 18) { "idCardCertCode length must be 18" }
    val allIdCards = userInfoRepo.findDisInfosByUserAccountIdAndIdCardCode(userAccountId, idCardCertCode)
    log.trace("[checkIdCardCertCodeInDisCertCodeOrThrow] allIdCards={}", allIdCards)
    if (allIdCards.isEmpty()) {
      log.trace("[checkIdCardCertCodeInDisCertCodeOrThrow] no disInfo found, return")
      return
    } else {
      val allIn = allIdCards.all { it.certCode?.startsWith(idCardCertCode) != false }
      log.trace("[checkIdCardCertCodeInDisCertCodeOrThrow] allIn={}", allIn)
      require(allIn) { "disInfoCertCode not in idCard" }
    }
  }

  /**
   * ## 根据用户 ID 更新昵称
   * > 通过指定的用户 ID 和昵称更新用户账户的昵称信息。
   * - 验证 `nickName` 是否为空，若为空则抛出异常。
   * - 调用 `userAccountRepo.update` 方法更新用户账户实体。
   * - 使用 Jimmer 的强类型 DSL 构造器创建更新对象。
   *
   * @param id 用户主键 ID，用于定位需要更新的用户账户。
   * @param nickName 新的昵称，必须非空且长度大于 0。
   * @return 返回更新后的 `UserAccount` 实体对象。
   */
  @ACID
  fun persistNickNameById(id: RefId, nickName: String): UserAccount {
    log.trace("[persistNickNameById] called with id={}, nickName={}", id, nickName)
    require(nickName.isNotBlank()) { "nickName is blank" }
    val result = userAccountRepo.save(
      UserAccount {
        this.id = id
        this.nickName = nickName
      },
      SaveMode.UPDATE_ONLY,
      AssociatedSaveMode.REPLACE
    )
    log.trace("[persistNickNameById] result={}", result)
    return result
  }

  /**
   * ## 管理员分页查询用户信息
   * > 支持银行卡类别等复杂条件
   */
  fun fetchUserInfosAsAdmin(spec: com.tnmaster.dto.userinfo.UserInfoAdminSpec): io.github.truenine.composeserver.domain.IPage<com.tnmaster.dto.userinfo.UserInfoAdminView> {
    log.trace("[fetchUserInfosAsAdmin] called with spec={}", spec)
    val result = userInfoRepo.sql.createQuery(UserInfo::class) {
      where(spec)
      spec.certsBankGroupValueIn?.takeIf { it.isNotEmpty() }?.apply {
        val existsBankCardAndBankNameIn = subQuery(BankCard::class) {
          where(
            table.bankName `valueIn?` spec.certsBankGroupValueIn,
            table.visible ne false,
            table.userAccountId eq parentTable.userAccountId
          )
          select(table.id)
        }
        val existsBankCardCert = subQuery(Cert::class) {
          where(
            table.doType eq CertTyping.BANK_CARD,
            table.visible ne false,
          )
          select(table.id)
        }
        where(
          exists(existsBankCardCert),
          exists(existsBankCardAndBankNameIn)
        )
      }
      orderBy(table.crd.desc())
      select(table.fetch(com.tnmaster.dto.userinfo.UserInfoAdminView::class))
    }.fetchPq(spec)
    log.trace("[fetchUserInfosAsAdmin] result={}", result)
    return result
  }
}
