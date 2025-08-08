package com.tnmaster.application.service

import cn.hutool.core.img.ImgUtil
import com.tnmaster.dto.bankcard.BankCardView
import com.tnmaster.dto.cert.*
import com.tnmaster.entities.*
import com.tnmaster.application.repositories.IBankCardRepo
import com.tnmaster.application.repositories.ICertRepo
import com.tnmaster.application.repositories.IUserInfoRepo
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.depend.servlet.toReadableAttachment
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.generator.IOrderCodeGenerator
import org.springframework.beans.factory.annotation.Qualifier
import io.github.truenine.composeserver.oss.ObjectStorageService
import io.github.truenine.composeserver.oss.PutObjectRequest
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import io.github.truenine.composeserver.enums.MediaTypes as MimeTypes
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.AuditTyping
import io.github.truenine.composeserver.rds.enums.CertContentTyping
import io.github.truenine.composeserver.rds.enums.CertPointTyping
import io.github.truenine.composeserver.rds.enums.CertPointTyping.*
import io.github.truenine.composeserver.rds.enums.CertTyping
import io.github.truenine.composeserver.rds.enums.CertTyping.*
import io.github.truenine.composeserver.rds.toFetcher
import io.github.truenine.composeserver.slf4j
import io.github.truenine.composeserver.toId
import org.babyfish.jimmer.Input
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.Font
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

@Service
class CertService(
  private val userInfoRepo: IUserInfoRepo,
  private val certRepo: ICertRepo,
  private val oss: ObjectStorageService,
  private val bankCardRepo: IBankCardRepo,
  private val attService: AttachmentService,
  @Qualifier("bizCode") private val bizCoder: IOrderCodeGenerator,
) {

  /**
   * ## 根据银行卡号查询银行卡是否存在
   * 通过银行卡号在数据库中查询是否存在对应的银行卡记录
   *
   * @param bankCardCode 银行卡号，用于查询银行卡记录
   * @return Boolean 返回是否存在对应的银行卡记录，存在返回true，否则返回false
   */
  fun foundBankCardByCode(
    bankCardCode: String,
  ): Boolean {
    log.trace("[foundBankCardByCode] called with bankCardCode={}", bankCardCode)
    val result = bankCardRepo.existsByCode(bankCardCode)
    log.trace("[foundBankCardByCode] result={}", result)
    return result
  }

  @ACID
  fun fixAllImagedCertTypingMarkForUserInfoOrAccount(
    userInfoId: RefId? = null, userAccountId: RefId? = null,
  ): List<Cert> {
    log.trace("[fixAllImagedCertTypingMarkForUserInfoOrAccount] called with userInfoId={}, userAccountId={}", userInfoId, userAccountId)
    require(
      userInfoId != null || userAccountId != null
    ) { "用户或用户信息至少需要其中一个" }
    val certs = certRepo.findCertsByUserAccountIdInOrUserInfoIdIn(
      userAccountIds = listOf(userAccountId), userInfoIds = listOf(userInfoId), fetcher = newFetcher(Cert::class).by {
        coType()
        doType()
        poType()
      })
    log.trace("[fixAllImagedCertTypingMarkForUserInfoOrAccount] fetched certs size={}, certs={}", certs.size, certs)
    val result = certs.map {
      it.copy {
        coType = when (coType) {
          CertContentTyping.SCREEN_SHOT, CertContentTyping.SCANNED_IMAGE, CertContentTyping.PROCESSED_SCANNED_IMAGE, CertContentTyping.PROCESSED_IMAGE -> coType

          CertContentTyping.COPYFILE_IMAGE, CertContentTyping.REMAKE_IMAGE, CertContentTyping.IMAGE -> CertContentTyping.PROCESSED_SCANNED_IMAGE

          CertContentTyping.VIDEO, CertContentTyping.RECORDING, CertContentTyping.PROCESSED_VIDEO, CertContentTyping.PROCESSED_AUDIO -> coType

          CertContentTyping.NONE, null -> null
        }
        doType = when (doType) {
          ID_CARD, ID_CARD2 -> ID_CARD2

          DISABILITY_CARD, DISABILITY_CARD2, DISABILITY_CARD3 -> DISABILITY_CARD3

          HOUSEHOLD_CARD, BANK_CARD, CONTRACT, BIZ_LICENSE, TITLE_IMAGE, PERSONAL_INCOME_TAX_VIDEO -> doType

          CertTyping.NONE, null -> null
        }
        poType = when (poType) {
          HEADS, TAILS, DOUBLE -> poType

          ALL, INTACT, ALL_CONTENT -> DOUBLE

          CertPointTyping.NONE, null -> null
        }
      }
    }.let {
      certRepo.saveAll(it)
    }
    log.trace("[fixAllImagedCertTypingMarkForUserInfoOrAccount] result size={}, result={}", result.size, result)
    return result
  }

  /**
   * # 持久化审核状态
   * > 批量更新证件审核状态，并同步更新相关用户信息
   *
   * 该函数执行以下操作：
   * 1. 过滤出未通过审核的证件
   * 2. 对于未通过审核的证件，根据证件类型清理相关用户信息
   * 3. 批量保存所有证件的审核状态
   *
   * @param auditCertList 待审核的证件列表，包含审核状态信息
   * @return 更新后的证件实体列表
   */
  @ACID
  fun persistAuditStatus(auditCertList: List<CertAdminPutAuditStatusDto>): List<Cert> {
    val notAuditStatuses = auditCertList.filterNot { it.auditStatus == AuditTyping.PASS }.mapNotNull { it.id?.toId() }
    if (notAuditStatuses.isNotEmpty()) {
      val allCerts = certRepo.findAllById(notAuditStatuses)
      allCerts.forEach {
        val accountId = checkNotNull(it.userId) { "userAccountId 不可为空" }
        val infoId = checkNotNull(it.userInfoId) { "userInfoId 不可为空" }
        when (it.doType) {
          ID_CARD2,
          ID_CARD,
          DISABILITY_CARD,
          DISABILITY_CARD2,
          DISABILITY_CARD3,
            -> {
            userInfoRepo.save(
              UserInfo {
                id = infoId
                idCard = null
                gender = null
                birthday = null
                firstName = null
                lastName = null
                this.addressCode = null
                account { id = accountId }
                disInfo = null
              })
          }

          else -> Unit
        }
      }
    }
    return certRepo.saveAll(auditCertList.toEntities())
  }

  /**
   * ## 获取未审核的证件列表
   * > 根据用户账户ID、用户信息ID和可见性条件，分页获取未审核的证件视图列表。
   *
   * 该函数用于查询未审核的证件信息，并支持分页功能。通过传入用户账户ID、用户信息ID和可见性条件，可以过滤出符合条件的未审核证件。 返回的结果是一个分页对象，包含了符合条件的证件视图列表。
   *
   * @param userAccountId 用户账户ID，可选参数，用于指定查询的用户账户ID。
   * @param userInfoId 用户信息ID，可选参数，用于指定查询的用户信息ID。
   * @param visible 可见性，可选参数，用于指定查询的证件是否可见。
   * @param pq 分页查询对象，可选参数，默认为最大分页，用于指定分页的参数。
   * @return 返回一个分页对象，包含符合条件的未审核证件视图列表，类型为 `IPage<CertView>`。
   */
  fun fetchNotAuditCerts(userAccountId: RefId? = null, userInfoId: RefId? = null, visible: Boolean? = null, pq: Pq? = Pq.DEFAULT_MAX): IPage<CertView> {
    log.trace("[fetchNotAuditCerts] called with userAccountId={}, userInfoId={}, visible={}, pq={}", userAccountId, userInfoId, visible, pq)
    val result = certRepo.findAllByUserAccountIdAndNotAuditCert(
      userAccountId = userAccountId,
      userInfoId = userInfoId,
      visible = visible,
      fetcher = CertView::class.toFetcher(),
      page = pq,
    ).transferTo { CertView(it) }
    log.trace("[fetchNotAuditCerts] result={}", result)
    return result
  }

  /**
   * ## 获取未审核证件的数量统计
   * > 根据查询条件统计未审核证件的数量
   *
   * 该函数用于统计符合指定条件的未审核证件的数量。通过传入的查询条件对象 `CertAdminSpec`， 并设置审核状态为 `AuditTyping.NONE`，来获取未审核证件的统计结果。
   *
   * @param spec 查询条件对象，包含证件查询的各种条件
   * @return 返回一个映射，键为用户ID，值为该用户未审核证件的数量
   */
  fun fetchNotAuditCounts(spec: CertAdminSpec): Map<RefId, Long> {
    log.trace("[fetchNotAuditCounts] called with spec={}", spec)
    val result = certRepo.countCertBySpec(spec.copy(hasAuditStatus = listOf(AuditTyping.NONE)))
    log.trace("[fetchNotAuditCounts] result={}", result)
    return result
  }

  fun fetchBankCertsByUserAccountId(userAccountId: RefId): List<BankCardView> {
    log.trace("[fetchBankCertsByUserAccountId] called with userAccountId={}", userAccountId)
    val result = bankCardRepo.findBankCardsByUserAccountId(userAccountId)
    log.trace("[fetchBankCertsByUserAccountId] result={}", result)
    return result
  }

  fun fetchHouseholdCardCertByUserAccountIdOrUserInfoId(userAccountId: RefId? = null, userInfoId: RefId? = null, visible: Boolean? = null): List<CertView> =
    fetchCertByUserAccountIdOrUserInfoId(HOUSEHOLD_CARD, userAccountId, userInfoId, visible)

  fun fetchDisCard2CertByUserAccountIdOrUserInfoId(userAccountId: RefId? = null, userInfoId: RefId? = null, visible: Boolean? = null): List<CertView> =
    fetchCertByUserAccountIdOrUserInfoId(DISABILITY_CARD2, userAccountId, userInfoId, visible)

  fun fetchTitleImageByUserAccountIdOrUserInfoId(userAccountId: RefId? = null, userInfoId: RefId? = null, visible: Boolean? = null): List<CertView> =
    fetchCertByUserAccountIdOrUserInfoId(TITLE_IMAGE, userAccountId, userInfoId, visible)

  @ACID
  fun deleteTitleImageByUserAccountIdOrUserInfoId(userAccountId: RefId, visible: Boolean? = null): Int =
    deleteAllCertByUserAccountId(TITLE_IMAGE, userAccountId, visible = visible)

  fun fetchIdCard2CertByUserAccountIdOrUserInfoId(userAccountId: RefId? = null, userInfoId: RefId? = null, visible: Boolean? = null): List<CertView> =
    fetchCertByUserAccountIdOrUserInfoId(ID_CARD2, userAccountId, userInfoId, visible = visible)

  /**
   * ## 根据用户账户ID或用户信息ID获取证件信息
   * > 通过用户账户ID或用户信息ID查询证件信息，并返回符合条件的证件视图列表。
   *
   * 该函数通过用户账户ID或用户信息ID查询证件信息，支持根据证件类型和可见性进行过滤。返回的证件信息将被转换为 `CertView` 视图对象。
   *
   * @param certType 证件类型，用于指定查询的证件类型。
   * @param userAccountId 用户账户ID，可选参数，用于指定查询的用户账户ID。
   * @param userInfoId 用户信息ID，可选参数，用于指定查询的用户信息ID。
   * @param visible 可见性，可选参数，用于指定查询的证件是否可见。
   * @return 返回符合条件的证件视图列表，类型为 `List<CertView>`。
   */
  fun fetchCertByUserAccountIdOrUserInfoId(
    certType: CertTyping,
    userAccountId: RefId? = null,
    userInfoId: RefId? = null,
    visible: Boolean? = null,
  ): List<CertView> {
    log.trace(
      "[fetchCertByUserAccountIdOrUserInfoId] called with certType={}, userAccountId={}, userInfoId={}, visible={}",
      certType,
      userAccountId,
      userInfoId,
      visible
    )
    val result = certRepo.findFirstCertByHeadTailDoubleAndUserAccountIdOrUserInfoId(
      certType = certType,
      visible = visible,
      userAccountId = userAccountId,
      userInfoId = userInfoId,
      fetcher = CertView::class.toFetcher(),
    ).let { listOf(it.first, it.second) }.filterNotNull().map { CertView(it) }
    log.trace("[fetchCertByUserAccountIdOrUserInfoId] result={}", result)
    return result
  }

  fun fetchWatermarkCertsByUserInfoId(
    userInfoId: RefId,
    visible: Boolean? = null,
  ): List<CertView> {
    log.trace("[fetchWatermarkCertsByUserInfoId] called with userInfoId={}, visible={}", userInfoId, visible)
    val info = userInfoRepo.findById(userInfoId, fetcher = newFetcher(UserInfo::class).by {
      account { account() }
    }).getOrNull()
    log.trace("[fetchWatermarkCertsByUserInfoId] fetched userInfo={}", info)
    val result = info?.let {
      val userAccountIds = if (it.account?.id != null) listOf(it.account!!.id) else emptyList()
      certRepo.findCertsByUserAccountIdInOrUserInfoIdIn(
        userAccountIds = userAccountIds,
        userInfoIds = listOf(it.id),
        visible = visible,
        fetcher = CertView::class.toFetcher(),
      ).map { CertView(it) }
    } ?: emptyList()
    log.trace("[fetchWatermarkCertsByUserInfoId] result={}", result)
    return result
  }

  /**
   * ## 根据ID列表获取水印附件组
   * > 通过ID列表和可见性条件查询水印附件组
   *
   * 该方法根据提供的ID列表和可见性条件，从证书仓库中获取对应的水印附件组。如果ID列表为空，则返回空映射。
   *
   * @param ids ID列表，类型为`List<RefId>`
   * @param visible 可见性条件，类型为`Boolean?`，默认为`null`
   * @return 返回一个映射，键为`RefId`，值为`Cert`
   */
  fun fetchWaterMarkerAttachmentGroupById(ids: List<RefId>, visible: Boolean? = null): Map<RefId, Cert> {
    log.trace("[fetchWaterMarkerAttachmentGroupById] called with ids={}, visible={}", ids, visible)
    if (ids.isEmpty()) {
      log.trace("[fetchWaterMarkerAttachmentGroupById] ids is empty, return emptyMap")
      return emptyMap()
    }
    val result = certRepo.fetchWaterMarkerAttachmentGroupById(ids, visible)
    log.trace("[fetchWaterMarkerAttachmentGroupById] result={}", result)
    return result
  }

  /**
   * ## 获取证件类型信息
   * > 根据证件类型、上传用户账户ID、候选单点类型和文件列表，生成证件管理DTO列表、文件列表和上传用户账户ID的三元组
   *
   * 该函数主要用于处理证件上传的逻辑，根据传入的文件列表生成相应的DTO对象，并确保上传用户账户ID的有效性。
   *
   * @param certType 证件类型，用于指定证件的具体类型
   * @param uploadUserAccountId 上传用户账户ID，用于关联上传的用户
   * @param candidateSinglePointType 候选单点类型，默认为双点类型，用于指定证件的单点类型
   * @param files 文件列表，包含上传的证件文件，至少需要一个文件
   * @return 返回一个三元组，包含生成的证件管理DTO列表、文件列表和上传用户账户ID
   * @throws IllegalArgumentException 如果文件列表为空或上传用户账户ID为空，将抛出此异常
   */
  private fun getCertType(
    certType: CertTyping,
    uploadUserAccountId: RefId,
    candidateSinglePointType: CertPointTyping = DOUBLE,
    files: List<MultipartFile?>,
  ): Triple<List<CertAdminPostDto>, List<MultipartFile>, RefId> {
    require(files.isNotEmpty()) { "文件列表不能为空" }
    val (head, tail) = files
    checkNotNull(head) { "首证件不能为空" }
    val fileList = if (tail != null) {
      listOf(head, tail)
    } else {
      listOf(head)
    }

    val dtoList = if (fileList.size == 1) {
      listOf(
        CertAdminPostDto(
          doType = certType,
          poType = candidateSinglePointType,
          coType = CertContentTyping.SCANNED_IMAGE,
          userAccountId = uploadUserAccountId.toString().also {
            // 确保 uploadUserAccountId 的转换和使用是安全的
            require(it.isNotBlank()) { "上传用户账户ID不能为空" }
          },
        )
      )
    } else {
      listOf(
        CertAdminPostDto(
          doType = certType,
          poType = HEADS,
          coType = CertContentTyping.SCANNED_IMAGE,
          userAccountId = uploadUserAccountId.toString().also { require(it.isNotBlank()) { "上传用户账户ID不能为空" } },
        ),
        CertAdminPostDto(
          doType = certType,
          poType = TAILS,
          coType = CertContentTyping.SCANNED_IMAGE,
          userAccountId = uploadUserAccountId.toString().also { require(it.isNotBlank()) { "上传用户账户ID不能为空" } },
        ),
      )
    }
    return Triple(dtoList, fileList, uploadUserAccountId)
  }

  /**
   * ## 删除身份证附件
   * 根据用户账户ID删除身份证附件信息，支持按可见性过滤
   *
   * @param userAccountId 用户账户ID，类型为RefId
   * @param visible 附件可见性，可选参数，默认为null表示不进行可见性过滤
   * @return 删除的记录数
   */
  @ACID
  fun deleteIdCard2Attachment(userAccountId: RefId, visible: Boolean? = null): Int {
    return deleteAllCertByUserAccountId(ID_CARD2, userAccountId, visible)
  }

  /**
   * ## 删除用户残疾证2附件
   * > 根据用户账户ID和可见性状态删除残疾证2附件
   *
   * 该函数用于删除指定用户的残疾证2附件，支持根据可见性状态进行过滤删除。
   *
   * @param userAccountId 用户账户ID，类型为RefId
   * @param visible 可见性状态，可选参数，默认为null
   * @return 删除的附件数量
   */
  @ACID
  fun deleteDisCard2AttachmentByUserAccountId(userAccountId: RefId, visible: Boolean? = null): Int {
    return deleteAllCertByUserAccountId(DISABILITY_CARD2, userAccountId, visible)
  }

  /**
   * ## 删除用户账户下的户口本附件
   * > 根据用户ID和可见性状态删除关联的户口本附件
   *
   * 该函数通过调用`deleteAllCertByUserAccountId`方法，删除指定用户账户下的户口本附件。 支持根据可见性状态进行条件删除。
   *
   * @param userId 用户ID，用于标识要删除附件的用户账户
   * @param visible 附件可见性状态，可选参数，默认为null表示不限制可见性
   * @return 返回删除的记录数
   */
  @ACID
  fun deleteHouseholdAttachmentByUserAccountId(userId: RefId, visible: Boolean? = null): Int {
    return deleteAllCertByUserAccountId(HOUSEHOLD_CARD, userId, visible)
  }

  /**
   * ## 根据用户账户ID删除所有证件
   * > 删除指定用户账户下的所有证件，可选择是否仅标记为不可见
   *
   * 该函数会根据用户账户ID查找所有关联的用户信息ID，然后根据证件类型和用户账户ID或用户信息ID查找所有证件。 如果 `visible` 参数为 `null`，则删除所有相关附件；否则，仅将证件标记为不可见。
   *
   * @param certType 证件类型
   * @param userAccountId 用户账户ID
   * @param visible 是否仅标记为不可见，默认为 `null` 表示直接删除
   * @return 删除或标记为不可见的证件数量
   */
  @ACID
  fun deleteAllCertByUserAccountId(certType: CertTyping, userAccountId: RefId, visible: Boolean? = null): Int {
    val userInfoIds = userInfoRepo.findIdsByUserAccountId(userAccountId)
    val certs = certRepo.findCertsByUserAccountIdInOrUserInfoIdInAndCertType(
      certType = certType,
      fetcher = CertAdminView::class.toFetcher(),
      userAccountIds = listOf(userAccountId),
      userInfoIds = userInfoIds,
    )
    if (visible == null) {
      certs.forEach {
        attService.effectDeleteById(it.metaAttachment.id)
        attService.effectDeleteById(it.waterMarkerAttachment!!.id)
      }
    }
    return certRepo.deleteOrVisibleCertByUserAccountIdOrUserInfoIdAndCertType(
      certType,
      userAccountIds = listOf(userAccountId),
      userInfoIds = userInfoIds,
      visible = visible,
    )
  }

  /**
   * ## 提交银行卡附件信息
   * > 处理用户提交的银行卡附件信息，包括用户基本信息、银行卡信息以及证件正反面照片。 函数首先保存用户的银行卡信息，然后处理并保存证件照片，最后返回银行卡实体。
   *
   * @param userAccountId 用户账户ID，用于标识用户
   * @param baseBankCard 银行卡基本信息，包含银行卡的代码、电话号码等
   * @param head 银行卡正面照片文件
   * @param tail 银行卡反面照片文件，可选参数
   * @param createInfo 证件创建信息，包含分组编码等元数据
   * @return 返回保存后的银行卡
   */
  @ACID
  fun postBankCardAttachment(
    userAccountId: RefId,
    baseBankCard: BankCard,
    head: MultipartFile,
    tail: MultipartFile? = null,
    createInfo: CertCreatedInfoDto,
  ): BankCard {
    val code = createInfo.groupCode ?: bizCoder.nextString()
    check(baseBankCard.phone?.isNotBlank() == true) { "银行卡手机号码不能为空" }
    val exists = bankCardRepo.existsByCode(baseBankCard.code)
    userInfoRepo.saveNotExistsPhoneByUserAccountId(userAccountId, baseBankCard.phone)
    check(!exists) { "银行卡已存在" }

    val savedCerts = postCertWaterMarkerImageAttachments(
      data = getCertType(
        certType = BANK_CARD,
        uploadUserAccountId = userAccountId,
        candidateSinglePointType = DOUBLE,
        files = listOf(head, tail),
      ),
      createInfo = createInfo,
    )
    return bankCardRepo.save(
      BankCard(base = baseBankCard) {
        userAccount { id = userAccountId }
        certs = savedCerts.map {
          Cert(base = it) {
            id = it.id
            this.groupCode = code
          }
        }
      }, SaveMode.INSERT_ONLY
    )
  }

  /**
   * ## 提交残疾证附件信息
   * 该函数用于处理用户提交的残疾证附件信息，包括用户基本信息、残疾信息以及证件正反面照片。 函数首先保存用户的残疾信息，然后处理并保存证件照片，最后返回证件视图列表。
   *
   * @param userAccountId 用户账户ID，用于标识用户
   * @param disInfoDto 残疾信息输入对象，包含用户的残疾信息
   * @param baseUserInfo 用户基本信息输入对象，可选参数，包含用户的基本信息
   * @param createInfo 证件创建信息DTO，包含证件的创建相关信息
   * @param head 证件正面照片文件，必须提供
   * @param tail 证件反面照片文件，可选参数
   * @return 返回处理后的证件视图列表，包含证件的详细信息
   */
  @ACID
  fun postDisCard2Attachment(
    userAccountId: RefId,
    disInfoDto: Input<DisInfo>,
    baseUserInfo: Input<UserInfo>? = null,
    createInfo: CertCreatedInfoDto,
    head: MultipartFile,
    tail: MultipartFile?,
  ): List<CertView> {
    userInfoRepo.saveDisInfoByUserAccountId(userAccountId, disInfoDto.toEntity(), base = baseUserInfo?.toEntity() ?: UserInfo {})
    return postHeadTailDoubleAttachmentByUserAccountId(DISABILITY_CARD2, userAccountId, head = head, tail = tail, createInfo = createInfo)
  }

  /**
   * ## 上传标题图片附件
   * > 处理用户上传的标题图片附件，支持单张或双张图片上传
   *
   * 该函数用于处理用户上传的标题图片附件，支持上传单张或双张图片，并生成相应的证件视图。 函数内部调用 `postHeadTailDoubleAttachmentByUserAccountId` 完成具体的业务逻辑。
   *
   * @param userAccountId 用户账户ID，用于标识上传附件的用户
   * @param head 必传的头部图片文件，通常为标题图片的主要部分
   * @param tail 可选的尾部图片文件，通常为标题图片的补充部分
   * @param createCertInfo 证件创建信息，包含创建证件所需的基本信息
   * @return 返回生成的证件视图列表，包含上传图片后的证件信息
   */
  @ACID
  fun postTitleImageAttachment(userAccountId: RefId, head: MultipartFile, tail: MultipartFile?, createCertInfo: CertCreatedInfoDto): List<CertView> {
    return postHeadTailDoubleAttachmentByUserAccountId(
      certType = TITLE_IMAGE,
      userAccountId = userAccountId,
      candidateSinglePointType = HEADS,
      head = head,
      tail = tail,
      createInfo = createCertInfo,
    )
  }

  /**
   * ## 提交户口本附件信息
   * > 该函数用于处理用户提交的户口本附件信息，包括正面和反面图片，并返回证件视图列表
   *
   * 该函数在事务中执行，确保数据一致性。通过调用内部方法处理具体的附件上传逻辑。
   *
   * @param userAccountId 用户账户ID，用于关联附件信息
   * @param head 户口本正面图片文件（必传）
   * @param tail 户口本反面图片文件（可选）
   * @param createInfo 证件创建信息DTO，包含证件相关的基础信息
   * @return 返回处理后的证件视图列表，包含上传后的证件信息
   */
  @ACID
  fun postHouseholdCardAttachment(userAccountId: RefId, head: MultipartFile, tail: MultipartFile?, createInfo: CertCreatedInfoDto): List<CertView> {
    return postHeadTailDoubleAttachmentByUserAccountId(
      certType = HOUSEHOLD_CARD,
      userAccountId = userAccountId,
      head = head,
      tail = tail,
      createInfo = createInfo,
    )
  }

  @ACID
  fun postHeadTailDoubleAttachmentByUserAccountId(
    certType: CertTyping,
    userAccountId: RefId,
    candidateSinglePointType: CertPointTyping = DOUBLE,
    head: MultipartFile,
    tail: MultipartFile?,
    createInfo: CertCreatedInfoDto,
  ): List<CertView> {
    deleteAllCertByUserAccountId(certType, userAccountId)
    return postCertWaterMarkerImageAttachments(
      data = getCertType(
        certType = certType,
        uploadUserAccountId = userAccountId,
        candidateSinglePointType = candidateSinglePointType,
        files = listOf(head, tail),
      ),
      createInfo = createInfo.copy(createUserId = userAccountId.toString()),
    ).map { CertView(it) }
  }

  /**
   * ## 上传身份证正反面附件
   * > 将用户身份证的正反面图片上传至附件系统，并关联到用户账户
   *
   * 该函数执行以下操作：
   * 1. 将用户信息输入转换为实体对象
   * 2. 保存身份证信息到用户账户
   * 3. 上传身份证正反面图片并生成证件视图
   *
   * @param userAccountId 用户账户ID
   * @param userInfoBase 用户基本信息输入对象
   * @param createInfo 证件创建信息DTO
   * @param head 身份证正面图片文件
   * @param tail 身份证反面图片文件（可选）
   * @return 返回包含证件视图的列表
   * @throws IllegalArgumentException 当 userInfoBase.toEntity() 转换失败时抛出
   */
  @ACID
  fun postIdCard2Attachment(
    userAccountId: RefId,
    userInfoBase: Input<UserInfo>,
    createInfo: CertCreatedInfoDto,
    head: MultipartFile,
    tail: MultipartFile?,
  ): List<CertView> {
    val baseEntity = userInfoBase.toEntity()
    userInfoRepo.saveIdCardByUserAccountId(userAccountId = userAccountId, idCard = baseEntity.idCard!!, base = baseEntity)
    return postHeadTailDoubleAttachmentByUserAccountId(
      certType = ID_CARD2,
      userAccountId = userAccountId,
      head = head,
      tail = tail,
      createInfo = createInfo,
    )
  }

  /**
   * ## 上传证件水印图片附件
   * > 该函数用于批量上传带有水印的证件图片附件，并返回处理后的证件信息列表
   *
   * 函数内部会生成分组编码（如果未提供），并调用底层方法完成实际的上传操作
   *
   * @param data 包含以下信息的Triple：
   *     - 第一个元素：证件信息DTO列表
   *     - 第二个元素：上传的多文件列表
   *     - 第三个元素：用户账户ID
   *
   * @param createInfo 证件创建信息DTO，包含分组编码等元数据
   * @return 处理后的证件实体列表
   * @throws IllegalArgumentException 当输入参数不合法时抛出
   */
  @ACID
  private fun postCertWaterMarkerImageAttachments(
    data: Triple<List<CertAdminPostDto>, List<MultipartFile>, RefId>,
    createInfo: CertCreatedInfoDto,
  ): List<Cert> {
    val code = createInfo.groupCode ?: bizCoder.nextString()
    return postCertWaterMarkerImageAttachments(
      certDtos = data.first,
      files = data.second,
      userAccountId = data.third,
      createInfo = createInfo.copy(groupCode = code),
    )
  }

  /**
   * ## 上传证件水印图片附件
   * > 批量上传证件图片并生成水印版本，保存到OSS并记录附件信息
   *
   * 该函数执行以下操作：
   * 1. 校验输入参数的有效性
   * 2. 为每个证件生成唯一标识
   * 3. 上传原始图片和水印图片到OSS
   * 4. 保存证件信息到数据库
   * 5. 返回保存后的证件信息
   *
   * @param certDtos 证件信息输入列表，使用Jimmer Input DSL
   * @param files 对应的证件图片文件列表
   * @param userAccountId 操作用户账号ID
   * @param createInfo 基础证件信息（可选），用于继承部分字段
   * @param groupCode 分组编码（可选），未提供时自动生成
   * @return 保存后的证件信息列表
   * @throws IllegalArgumentException 当以下情况发生时抛出：
   *     - certDtos与files数量不匹配
   *     - 证件信息中用户信息为空
   */
  @ACID
  fun postCertWaterMarkerImageAttachments(
    certDtos: List<CertAdminPostDto>,
    files: List<MultipartFile>,
    userAccountId: RefId,
    createInfo: CertCreatedInfoDto,
  ): List<Cert> {
    val code = createInfo.groupCode ?: bizCoder.nextString()
    if (certDtos.isEmpty() || files.isEmpty()) return emptyList()
    require(certDtos.size == files.size) { "certs.size != files.size" }
    require(certDtos.all {
      (it.userInfoId != null || it.userAccountId != null) && (it.userInfoId != it.userAccountId)
    }) { "证件组中存在用户信息为空的证件" }

    // 文件有效性校验提前
    files.forEach { file ->
      require(file.contentType != null) { "文件类型不能为空" }
      require(file.contentType!!.startsWith("image/")) { "不支持的文件类型：${file.contentType}" }
      require(file.bytes.isNotEmpty()) { "无法读取图片文件，可能是不支持的图片格式或文件已损坏" }
    }

    val userInfoId = userInfoRepo.findFirstIdByUserAccountId(userAccountId)
    require(
      userInfoId != userAccountId
    ) {
      "账号id $userAccountId 与 用户信息 id $userInfoId 完全一致"
    }

    val pairs = certDtos.zip(files)
    return pairs.map { (dto, file) ->
      val attCode = bizCoder.nextString()
      val att = attService.recordUpload(file.toReadableAttachment()) { fr ->
        val ossFile = runBlocking {
          oss.putObject(
            PutObjectRequest(
              bucketName = META_CERT,
              objectName = attCode,
              inputStream = fr.inputStream!!,
              size = fr.size,
              contentType = fr.mimeType!!
            )
          ).getOrThrow()
        }
        AttachmentService.ComputedUploadRecord(
          baseUrl = oss.exposedBaseUrl,
          baseUri = META_CERT,
          metaName = fr.name,
          saveName = attCode,
          mimeType = MimeTypes[fr.mimeType!!]!!,
          size = ossFile.size,
        )
      }
      val wmAtt = attService.recordUpload(file.toReadableAttachment()) { fr ->
        val gf = generateWatermark(fr.inputStream!!, attCode, fr.mimeType)
        val uploadOssFile = runBlocking {
          oss.putObject(
            PutObjectRequest(
              bucketName = WM,
              objectName = attCode,
              inputStream = ByteArrayInputStream(gf),
              size = gf.size.toLong(),
              contentType = file.contentType!!
            )
          ).getOrThrow()
        }
        AttachmentService.ComputedUploadRecord(
          baseUrl = oss.exposedBaseUrl,
          baseUri = uploadOssFile.bucketName,
          saveName = uploadOssFile.objectName,
          metaName = fr.name,
        )
      }

      val saved = certRepo.insert(
        Cert(base = createInfo.toEntity()) {
          groupCode = code
          createIp = createInfo.createIp
          createDeviceId = createInfo.createDeviceId
          createDatetime = datetime.now()
          doType = dto.doType
          poType = dto.poType
          coType = dto.coType
          this.userId = dto.userAccountId?.toId()
          this.userInfoId = dto.userInfoId?.toId() ?: userInfoId
          auditStatus = AuditTyping.NONE
          wmCode = attCode
          attId = att.id
          wmAttId = wmAtt.id
          createUserId = userAccountId
        })
      certRepo.findById(saved.id, CertView::class.toFetcher()).get()
    }
  }

  /**
   * ## 根据用户账户ID和银行卡ID删除银行卡附件
   * > 删除指定用户账户下的银行卡附件，包括元数据附件和水印附件
   *
   * 该函数首先通过用户账户ID和银行卡ID查询银行卡及其关联的证件信息，然后删除所有关联的证件及其附件，最后删除银行卡记录。
   *
   * @param userAccountId 用户账户ID
   * @param bankCardId 银行卡ID
   */
  @ACID
  fun deleteBankCardAttachmentByUserAccountId(userAccountId: RefId, bankCardId: RefId) {
    val bankCardCerts = bankCardRepo.findFirstByIdAndUserAccountId(
      userAccountId,
      bankCardId,
      newFetcher(BankCard::class).by {
        certs {
          metaAttachment {
            parentUrlAttachment { baseUri() }
            saveName()
          }
          waterMarkerAttachment {
            parentUrlAttachment { baseUri() }
            saveName()
          }
        }
      },
    )?.certs?.also { certRepo.deleteAll(it) }

    bankCardCerts?.forEach { attService.effectDeleteAllById(listOf(it.metaAttachment.id, it.waterMarkerAttachment!!.id)) }

    bankCardRepo.deleteById(bankCardId)
  }

  companion object {
    @JvmStatic
    private val log = slf4j<CertService>()

    @JvmStatic
    private fun generateWatermark(
      md: InputStream,
      text: String,
      mediaType: String? = "image/png",
      yOffsetFontSize: Int = 0,
      xOffsetFontSize: Int = 0,
      alpha: Float = 0.1f,
      color: Color = Color.BLACK,
      maxFontSize: Int = 100,
    ): ByteArray {
      val type = MimeTypes.findVal(mediaType!!)!!.ext
      return md.use { i ->
        val bf = ImageIO.read(i)
        require(bf != null) { "无法读取图片文件，可能是不支持的图片格式或文件已损坏" }

        val width = bf.width
        val height = bf.height
        val textLength = text.length

        val t = width / textLength - 1
        val f = height / textLength - 1
        val fontSize = min(min(t, f), maxFontSize)

        val img = ImgUtil.pressText(bf, text, color, Font(null, Font.BOLD, fontSize), xOffsetFontSize * fontSize, yOffsetFontSize * fontSize, alpha)
        ImgUtil.toBytes(img, type)
      }
    }

    private const val META_CERT = "meta-certs"
    private const val WM = "watermarks"
  }
}
