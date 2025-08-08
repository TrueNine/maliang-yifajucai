package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckLogin
import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.dto.bankcard.BankCardDto
import com.tnmaster.dto.bankcard.BankCardView
import com.tnmaster.dto.cert.CertAdminPostDto
import com.tnmaster.dto.cert.CertAdminPutAuditStatusDto
import com.tnmaster.dto.cert.CertAdminSpec
import com.tnmaster.dto.cert.CertCreatedInfoDto
import com.tnmaster.dto.cert.CertView
import com.tnmaster.dto.disinfo.DisInfoPostMeDto
import com.tnmaster.dto.userinfo.UserInfoPutDto
import com.tnmaster.entities.Bank
import com.tnmaster.entities.BankCard
import com.tnmaster.entities.Cert
import com.tnmaster.service.BankService
import com.tnmaster.service.CertService
import com.tnmaster.service.UserAccountService
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.datetime
import io.github.truenine.composeserver.domain.AuthRequestInfo
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.toId
import org.babyfish.jimmer.client.ApiIgnore
import org.babyfish.jimmer.client.meta.Api
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * # 证件管理 API
 * > 此控制器提供了与证件相关的各种操作接口，包括证件信息的获取、上传、删除等。
 *
 * 该控制器主要分为两类接口：
 * 1. 管理员端接口，需要具备 ADMIN 权限才能调用。
 * 2. 用户端接口，需要用户登录后才能调用。
 *
 * @author TrueNine
 * @since 2025-03-23
 */
@Api
@RequestMapping("v2/cert")
@RestController
class CertV2Api(
  private val bankService: BankService,
  private val certService: CertService,
  private val userAccountService: UserAccountService,
) {
  /**
   * 对现有的证件序列标记，进行压缩收紧处理
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PatchMapping("compress_cert_marker")
  fun patchCertTypingMarkersAsAdmin(
    @RequestParam userInfoId: RefId?,
    @RequestParam userAccountId: RefId?,
  ): List<Cert> {
    return certService.fixAllImagedCertTypingMarkForUserInfoOrAccount(userInfoId, userAccountId)
  }

  /**
   * ## 批量更新证件审核状态（管理员权限）
   * > 管理员批量更新证件的审核状态，将新的审核状态持久化到数据库中。
   *
   * 该接口用于管理员批量更新证件的审核状态，需要管理员权限才能调用。 接收一个包含证件审核状态信息的列表，调用 `certService` 的 `persistAuditStatus` 方法将其保存到数据库中。 最终返回保存后的证件实体列表。
   *
   * @param authInfo 认证请求信息，包含当前用户的ID等信息
   * @param auditList 包含证件审核状态信息的列表，每个元素为 `CertAdminPutAuditStatusDto` 类型
   * @return 返回保存后的证件实体列表，类型为 `List<Cert>`
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PatchMapping("audit_statuses")
  fun patchCertsAuditStatusesAsAdmin(@ApiIgnore authInfo: AuthRequestInfo, @RequestBody auditList: List<CertAdminPutAuditStatusDto>): List<Cert> {
    return certService.persistAuditStatus(auditList)
  }

  /**
   * ## 获取未审核的证件信息列表
   *
   * 此接口供管理员使用，用于获取符合特定条件的未审核证件信息列表 必须具备 ADMIN 权限才能调用此接口
   *
   * @param spec 用于指定查询条件的对象，包括用户信息 ID、用户账户 ID、可见性等
   * @return 返回一个包含未审核证件视图的分页对象
   *
   * 注意：调用此接口时，必须提供用户信息 ID 或用户账户 ID，否则将抛出异常
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("not_audit_certs")
  fun getNotAuditCertsAsAdmin(spec: CertAdminSpec): IPage<CertView> {
    require(!spec.hasUserInfoIds.isNullOrEmpty() || !spec.hasUserAccountIds.isNullOrEmpty()) { "用户 id 或 信息 id 不能为空" }
    return certService.fetchNotAuditCerts(
      userInfoId = spec.hasUserInfoIds?.firstOrNull()?.toId(),
      userAccountId = spec.hasUserAccountIds?.firstOrNull()?.toId(),
      visible = spec.visible,
      pq = spec,
    )
  }

  /**
   * ## 获取银行列表接口
   * > 提供银行列表数据
   *
   * 该接口用于获取系统中所有银行的列表信息
   *
   * @return 返回银行实体列表
   */
  @Api
  @SaCheckLogin
  @GetMapping("banks")
  fun getBanks(): List<Bank> {
    return bankService.fetchAllBanks()
  }

  /**
   * ## 删除用户银行卡附件
   * > 删除当前登录用户的指定银行卡附件信息
   *
   * 该接口用于用户删除自己绑定的银行卡附件，需要用户已登录状态。
   *
   * @param authInfo 用户认证信息（自动注入，无需前端传递）
   * @param bankCardId 银行卡ID，用于指定要删除的银行卡附件
   * @author
   * @since yyyy-MM-dd
   */
  @Api
  @SaCheckLogin
  @DeleteMapping("me/bank_card_attachment")
  fun deleteBankCardAttachmentAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestParam bankCardId: RefId,
  ) {
    certService.deleteBankCardAttachmentByUserAccountId(authInfo.userId, bankCardId)
  }

  /**
   * ## 获取未审核证件数量
   * > 根据管理员查询条件统计各类证件的未审核数量
   *
   * 该接口用于管理员查看系统中各类证件的待审核数量统计，支持按证件类型、提交时间等条件筛选。
   *
   * @param spec 证件管理查询条件，包含证件类型、时间范围等过滤条件
   * @return 以证件类型ID为key，未审核数量为value的映射表
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("not_audit_cert_counts")
  fun getNotAuditCertCounts(spec: CertAdminSpec): Map<RefId, Long> {
    return certService.fetchNotAuditCounts(spec)
  }

  /**
   * ## 获取当前用户的所有银行卡信息
   *
   * 此接口用于获取经过身份验证的用户所关联的所有银行卡信息它返回一个包含多个[BankCardView]对象的列表， 其中[BankCardView]代表用户每一张银行卡的详细信息此方法通过用户账户ID从认证信息中提取参数， 并委托给[certService]处理实际的数据获取逻辑
   *
   * @param authInfo 认证请求信息，包含用户身份信息此参数由系统自动注入，用于识别和验证用户身份
   * @return 返回一个[BankCardView]对象列表，每个对象代表用户的一张银行卡信息
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/bank_card_attachment")
  fun getBankCardsAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
  ): List<BankCardView> {
    return certService.fetchBankCertsByUserAccountId(authInfo.userId)
  }

  /**
   * ## 上传银行卡附件
   * > 用户上传银行卡信息及附件图片，用于实名认证等场景
   *
   * 该接口需要用户登录，上传内容包括：
   * - 银行卡基本信息
   * - 银行卡正面照片（必传）
   * - 银行卡反面照片（可选）
   *
   * @param authInfo 用户认证信息（自动注入，无需前端传递）
   * @param dto 银行卡信息DTO，包含银行卡号、手机号、银行类型等
   * @param head 银行卡正面照片（MultipartFile）
   * @param tail 银行卡反面照片（MultipartFile，可选）
   * @return 保存后的银行卡信息实体
   * @throws IllegalArgumentException 当必填参数为空时抛出
   */
  @Api
  @SaCheckLogin
  @PostMapping("me/bank_card_attachment")
  fun postBankCardAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestPart dto: BankCardDto,
    @RequestPart head: MultipartFile,
    @RequestPart(required = false) tail: MultipartFile?,
  ): BankCard {
    checkNotNull(dto.code) { "银行卡号不能为空" }
    checkNotNull(dto.phone) { "手机号不能为空" }
    checkNotNull(dto.bankName) { "银行类型不能为空" }
    require(!certService.foundBankCardByCode(dto.code)) { "银行卡已存在，无法添加" }
    return certService.postBankCardAttachment(
      authInfo.userId,
      dto.toEntity(),
      head,
      tail,
      CertCreatedInfoDto(
        createDeviceId = authInfo.deviceId,
        createIp = authInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authInfo.userId.toString(),
      ),
    )
  }

  /**
   * ## 上传用户户口本附件
   * > 用于当前登录用户上传户口本附件信息
   *
   * 该接口支持上传户口本首页和尾页，尾页为可选参数。上传成功后返回证件视图信息。
   *
   * @param authInfo 认证信息（自动注入，API文档忽略）
   * @param head 户口本首页（必传）
   * @param tail 户口本尾页（可选）
   * @return 证件视图信息列表
   * @author
   * @since yyyy-MM-dd
   */
  @Api
  @SaCheckLogin
  @PostMapping("me/household_card_attachment")
  fun postHouseholdCardAttachmentAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestPart head: MultipartFile,
    @RequestPart(required = false) tail: MultipartFile?,
  ): List<CertView> {
    return certService.postHouseholdCardAttachment(
      authInfo.userId,
      head,
      tail,
      CertCreatedInfoDto(
        createDeviceId = authInfo.deviceId,
        createIp = authInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authInfo.userId.toString(),
      ),
    )
  }

  /**
   * ## 获取当前用户的户口本附件信息
   * > 该接口用于获取已登录用户的户口本附件信息，需要用户登录态
   *
   * 通过用户ID查询其户口本附件信息，仅返回可见的附件记录
   *
   * @param authInfo 用户认证信息，包含当前登录用户ID
   * @return 户口本附件视图列表，包含附件的基本信息
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/household_card_attachment")
  fun getHouseHoldCardAttachmentAsMe(@ApiIgnore authInfo: AuthRequestInfo): List<CertView> {
    return certService.fetchHouseholdCardCertByUserAccountIdOrUserInfoId(authInfo.userId, visible = true)
  }

  /**
   * ## 删除身份证附件二
   * > 用户自主删除第二身份证附件
   *
   * 本接口用于用户删除已上传的第二身份证附件信息，操作后附件将从系统中**永久删除**。
   *
   * 安全要求：
   * - 需用户登录态（通过`@SaCheckLogin`实现）
   * - 仅允许操作本人证件信息
   *
   * @param authInfo 请求认证信息（通过拦截器自动注入） 包含当前登录用户的`userId`
   * @return 通用空响应体 `Response<Unit>`
   */
  @Api
  @SaCheckLogin
  @DeleteMapping("me/id_card_2_attachment")
  fun deleteIdCard2AttachmentAsMe(@ApiIgnore authInfo: AuthRequestInfo) {
    certService.deleteIdCard2Attachment(authInfo.userId, visible = false)
  }

  /**
   * ## 获取自身的二代身份证
   *
   * @return 身份证附件集合，最多两张
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/id_card_2_attachment")
  fun getIdCard2AttachmentAsMe(@ApiIgnore authInfo: AuthRequestInfo): List<CertView> {
    return certService.fetchIdCard2CertByUserAccountIdOrUserInfoId(authInfo.userId, visible = true)
  }

  /**
   * ## 上传个人的身份证
   * > 此接口用于上传用户的二代身份证图片，并验证身份证号是否符合相关规则。
   * - 验证 `userInfoBase.idCard` 是否为空，若为空则抛出异常。
   * - 确保身份证号长度为 18 位，否则抛出异常。
   * - 调用 `userAccountService.checkIdCardCertCodeInDisCertCodeOrThrow` 方法检查身份证号是否存在于残疾证号中。
   * - 最后调用 `certService.postIdCard2Attachment` 方法完成身份证附件的上传。
   *
   * @param authInfo 用户认证信息，包含用户身份信息。此参数由系统自动注入。
   * @param userInfoBase 包含用户基本信息的对象，必须包含有效的身份证号。
   * @param head 身份证正面图片文件。
   * @param tail 身份证反面图片文件（可选）。
   * @return 返回一个包含上传成功的身份证视图对象列表。
   */
  @Api
  @SaCheckLogin
  @PostMapping("me/id_card_2_attachment")
  fun postIdCard2CertAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestPart userInfoBase: UserInfoPutDto,
    @RequestPart head: MultipartFile,
    @RequestPart(required = false) tail: MultipartFile?,
  ): List<CertView> {
    checkNotNull(userInfoBase.idCard) { "身份证不能为空" }
    require(userInfoBase.idCard.isNotBlank()) { "身份证不能为空" }
    require(userInfoBase.idCard.length == 18) { "身份证长度必须为18位" }
    userAccountService.checkIdCardCertCodeInDisCertCodeOrThrow(userAccountId = authInfo.userId, idCardCertCode = userInfoBase.idCard)
    return certService.postIdCard2Attachment(
      userAccountId = authInfo.userId,
      userInfoBase = userInfoBase,
      head = head,
      tail = tail,
      createInfo = CertCreatedInfoDto(
        createDeviceId = authInfo.deviceId,
        createIp = authInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authInfo.userId.toString(),
      ),
    )
  }

  /**
   * ## 删除用户标题图片附件
   * > 删除当前登录用户的标题图片附件
   *
   * 该接口用于删除用户上传的标题图片附件，仅限已登录用户使用。
   * 删除操作会将附件标记为不可见，而非物理删除。
   *
   * @param authInfo 用户认证信息，包含当前登录用户的ID
   * @see CertService.deleteTitleImageByUserAccountIdOrUserInfoId
   */
  @Api
  @SaCheckLogin
  @DeleteMapping("me/title_image_attachment")
  fun deleteTitleImageCertAsMe(@ApiIgnore authInfo: AuthRequestInfo) {
    certService.deleteTitleImageByUserAccountIdOrUserInfoId(authInfo.userId, visible = false)
  }

  /**
   * ## 获取当前用户的标题图片附件
   * > 该接口用于获取当前登录用户的标题图片附件信息
   *
   * 通过用户ID查询可见的标题图片附件，返回认证视图列表
   * @param authInfo 认证请求信息（自动注入，无需手动传递）
   * @return 认证视图列表，包含标题图片附件信息
   */
  @Api
  @SaCheckLogin
  @GetMapping("me/title_image_attachment")
  fun getTitleImageCertAsMe(@ApiIgnore authInfo: AuthRequestInfo): List<CertView> {
    return certService.fetchTitleImageByUserAccountIdOrUserInfoId(authInfo.userId, visible = true)
  }

  /** ## 上传自身寸照 */
  @Api
  @SaCheckLogin
  @PostMapping("me/title_image_attachment")
  fun postTitleImageCertAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestPart head: MultipartFile,
    @RequestPart(required = false) tail: MultipartFile?,
  ): List<CertView> {
    return certService.postTitleImageAttachment(
      authInfo.userId,
      head,
      tail,
      CertCreatedInfoDto(
        createDeviceId = authInfo.deviceId,
        createIp = authInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authInfo.userId.toString(),
      ),
    )
  }

  /** ## 获取自身残疾证信息 */
  @Api
  @SaCheckLogin
  @GetMapping("me/dis_card_2_attachment")
  fun getDisCard2AttachmentAsMe(@ApiIgnore authInfo: AuthRequestInfo): List<CertView> {
    return certService.fetchDisCard2CertByUserAccountIdOrUserInfoId(authInfo.userId, visible = true)
  }

  /**
   * ## 上传自身的残疾证
   *
   * @param head 残疾证正面或双面
   * @param tail 残疾证反面
   */
  @Api
  @SaCheckLogin
  @PostMapping("me/dis_card_2_attachment")
  fun postDisCard2AttachmentAsMe(
    @ApiIgnore authInfo: AuthRequestInfo,
    @RequestPart disInfoDto: DisInfoPostMeDto,
    @RequestPart userInfoBase: UserInfoPutDto,
    @RequestPart head: MultipartFile,
    @RequestPart(required = false) tail: MultipartFile?,
  ): List<CertView> {
    require(disInfoDto.certCode?.isNotBlank() == true) { "残疾证不能为空" }
    checkNotNull(disInfoDto.certCode) { "残疾证不能为空" }
    require(disInfoDto.certCode.isNotBlank()) { "残疾证不能为空" }
    userAccountService.checkDisInfoCertCodeContainsIdCardOrThrow(authInfo.userId, disInfoDto.certCode)
    return certService.postDisCard2Attachment(
      userAccountId = authInfo.userId,
      disInfoDto = disInfoDto,
      baseUserInfo = userInfoBase,
      head = head,
      tail = tail,
      createInfo = CertCreatedInfoDto(
        createDeviceId = authInfo.deviceId,
        createIp = authInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authInfo.userId.toString(),
      ),
    )
  }

  /**
   * ## 删除自身上传的户口卡
   *
   * @param authInfo 认证信息
   */
  @Api
  @SaCheckLogin
  @DeleteMapping("me/household_card_attachment")
  fun deleteHouseholdAttachmentByUserAccountId(@ApiIgnore authInfo: AuthRequestInfo) {
    certService.deleteHouseholdAttachmentByUserAccountId(authInfo.userId, visible = false)
  }

  /**
   * ## 删除残疾证附件（个人操作）
   * > 用户删除自己的残疾证附件信息
   *
   * 该接口用于用户删除自己上传的残疾证附件，实际执行逻辑为将附件标记为不可见（软删除）
   *
   * @param authInfo 认证信息（自动注入，无需手动传递）
   *     - 包含当前登录用户的ID信息
   *     - 通过`@ApiIgnore`注解避免在API文档中显示
   */
  @Api
  @SaCheckLogin
  @DeleteMapping("me/dis_card_2_attachment")
  fun deleteDisCard2AttachmentAsMe(@ApiIgnore authInfo: AuthRequestInfo) {
    certService.deleteDisCard2AttachmentByUserAccountId(authInfo.userId, visible = false)
  }

  /**
   * ## 根据 id 批量查询对应的水印证件文件
   *
   * @param ids 证件 id 集合
   * @return 水印证件文件集合 id to 水印证件文件
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("watermark_attachments_id_map")
  fun getWatermarkAttachmentsGroupByIds(@RequestParam ids: List<RefId>): Map<RefId, Cert> {
    return certService.fetchWaterMarkerAttachmentGroupById(ids)
  }

  /**
   * ## 根据 userInfoId 查询对应的水印证件文件
   * > 根据用户信息 id 查询对应的水印证件文件
   *
   * 该接口用于根据用户信息 id 查询对应的水印证件文件，返回认证视图列表
   *
   * @param userInfoId 用户信息 id
   * @return 水印证件文件集合
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("user_info_watermark_certs")
  fun getUserInfoWatermarkCerts(
    @RequestParam userInfoId: RefId,
  ): List<CertView> {
    return certService.fetchWatermarkCertsByUserInfoId(userInfoId)
  }

  /**
   * ## 批量上传证件信息（管理员权限）
   * > 管理员批量上传证件信息，并关联文件附件
   *
   * 该接口用于管理员批量上传证件信息，每个证件信息可以关联多个文件附件。 需要管理员权限才能调用。
   *
   * @param certs 证件信息列表，包含证件的基本信息
   * @param files 文件附件列表，每个文件对应一个证件信息
   * @param authRequestInfo 认证请求信息，包含当前用户的ID等信息
   * @return 返回上传成功的证件信息列表
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PostMapping("batch_certs")
  fun postCertsAsAdmin(
    @RequestPart certs: List<CertAdminPostDto>,
    @RequestPart files: List<MultipartFile>,
    @ApiIgnore authRequestInfo: AuthRequestInfo,
  ): List<Cert> {
    return certService.postCertWaterMarkerImageAttachments(
      certs,
      files,
      authRequestInfo.userId,
      CertCreatedInfoDto(
        createDeviceId = authRequestInfo.deviceId,
        createIp = authRequestInfo.currentIpAddr,
        createDatetime = datetime.now(),
        createUserId = authRequestInfo.userId.toString(),
      ),
    )
  }
}
