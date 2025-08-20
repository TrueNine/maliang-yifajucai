import type { Executor } from '../'
import type { Dynamic_Bank, Dynamic_BankCard, Dynamic_Cert } from '../model/dynamic/'
import type {
  BankCardDto,
  BankCardView,
  CertAdminPostDto,
  CertAdminPutAuditStatusDto,
  CertAdminSpec,
  CertView,
  DisInfoPostMeDto,
  IPage,
  UserInfoPutDto,
} from '../model/static/'

/**
 * # 证件管理 API
 * > 此控制器提供了与证件相关的各种操作接口，包括证件信息的获取、上传、删除等。
 *
 * 该控制器主要分为两类接口：
 * 1. 管理员端接口，需要具备 ADMIN 权限才能调用。
 * 2. 用户端接口，需要用户登录后才能调用。
 *
 */
export class CertV2Api {
  constructor(private executor: Executor) {}

  /**
   * ## 删除用户银行卡附件
   * > 删除当前登录用户的指定银行卡附件信息
   *
   * 该接口用于用户删除自己绑定的银行卡附件，需要用户已登录状态。
   *
   * @parameter {CertV2ApiOptions['deleteBankCardAttachmentAsMe']} options
   * - authInfo 用户认证信息（自动注入，无需前端传递）
   * - bankCardId 银行卡ID，用于指定要删除的银行卡附件
   */
  readonly deleteBankCardAttachmentAsMe: (options: CertV2ApiOptions['deleteBankCardAttachmentAsMe']) => Promise<
    void
  > = async (options) => {
    let _uri = '/v2/cert/me/bank_card_attachment'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.bankCardId
    _uri += _separator
    _uri += 'bankCardId='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 删除残疾证附件（个人操作）
   * > 用户删除自己的残疾证附件信息
   *
   * 该接口用于用户删除自己上传的残疾证附件，实际执行逻辑为将附件标记为不可见（软删除）
   *
   * @parameter {CertV2ApiOptions['deleteDisCard2AttachmentAsMe']} options
   * - authInfo 认证信息（自动注入，无需手动传递）
   *     - 包含当前登录用户的ID信息
   *     - 通过`@ApiIgnore`注解避免在API文档中显示
   */
  readonly deleteDisCard2AttachmentAsMe: () => Promise<
    void
  > = async () => {
    const _uri = '/v2/cert/me/dis_card_2_attachment'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 删除自身上传的户口卡
   *
   * @parameter {CertV2ApiOptions['deleteHouseholdAttachmentByUserAccountId']} options
   * - authInfo 认证信息
   */
  readonly deleteHouseholdAttachmentByUserAccountId: () => Promise<
    void
  > = async () => {
    const _uri = '/v2/cert/me/household_card_attachment'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 删除身份证附件二
   * > 用户自主删除第二身份证附件
   *
   * 本接口用于用户删除已上传的第二身份证附件信息，操作后附件将从系统中**永久删除**。
   *
   * 安全要求：
   * - 需用户登录态（通过`@RequireLogin`实现）
   * - 仅允许操作本人证件信息
   *
   * @parameter {CertV2ApiOptions['deleteIdCard2AttachmentAsMe']} options
   * - authInfo 请求认证信息（通过拦截器自动注入） 包含当前登录用户的`userId`
   * @return 通用空响应体 `Response<Unit>`
   */
  readonly deleteIdCard2AttachmentAsMe: () => Promise<
    void
  > = async () => {
    const _uri = '/v2/cert/me/id_card_2_attachment'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 删除用户标题图片附件
   * > 删除当前登录用户的标题图片附件
   *
   * 该接口用于删除用户上传的标题图片附件，仅限已登录用户使用。
   * 删除操作会将附件标记为不可见，而非物理删除。
   *
   * @parameter {CertV2ApiOptions['deleteTitleImageCertAsMe']} options
   * - authInfo 用户认证信息，包含当前登录用户的ID
   */
  readonly deleteTitleImageCertAsMe: () => Promise<
    void
  > = async () => {
    const _uri = '/v2/cert/me/title_image_attachment'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 获取当前用户的所有银行卡信息
   *
   * 此接口用于获取经过身份验证的用户所关联的所有银行卡信息它返回一个包含多个[BankCardView]对象的列表， 其中[BankCardView]代表用户每一张银行卡的详细信息此方法通过用户账户ID从认证信息中提取参数， 并委托给[certService]处理实际的数据获取逻辑
   *
   * @parameter {CertV2ApiOptions['getBankCardsAsMe']} options
   * - authInfo 认证请求信息，包含用户身份信息此参数由系统自动注入，用于识别和验证用户身份
   * @return 返回一个[BankCardView]对象列表，每个对象代表用户的一张银行卡信息
   */
  readonly getBankCardsAsMe: () => Promise<
    Array<BankCardView>
  > = async () => {
    const _uri = '/v2/cert/me/bank_card_attachment'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<BankCardView>>
  }

  /**
   * ## 获取银行列表接口
   * > 提供银行列表数据
   *
   * 该接口用于获取系统中所有银行的列表信息
   *
   * @return 返回银行实体列表
   */
  readonly getBanks: () => Promise<
    Array<Dynamic_Bank>
  > = async () => {
    const _uri = '/v2/cert/banks'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<Dynamic_Bank>>
  }

  /**
   * ## 获取自身残疾证信息
   */
  readonly getDisCard2AttachmentAsMe: () => Promise<
    Array<CertView>
  > = async () => {
    const _uri = '/v2/cert/me/dis_card_2_attachment'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<CertView>>
  }

  /**
   * ## 获取当前用户的户口本附件信息
   * > 该接口用于获取已登录用户的户口本附件信息，需要用户登录态
   *
   * 通过用户ID查询其户口本附件信息，仅返回可见的附件记录
   *
   * @parameter {CertV2ApiOptions['getHouseHoldCardAttachmentAsMe']} options
   * - authInfo 用户认证信息，包含当前登录用户ID
   * @return 户口本附件视图列表，包含附件的基本信息
   */
  readonly getHouseHoldCardAttachmentAsMe: () => Promise<
    Array<CertView>
  > = async () => {
    const _uri = '/v2/cert/me/household_card_attachment'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<CertView>>
  }

  /**
   * ## 获取自身的二代身份证
   *
   * @return 身份证附件集合，最多两张
   */
  readonly getIdCard2AttachmentAsMe: () => Promise<
    Array<CertView>
  > = async () => {
    const _uri = '/v2/cert/me/id_card_2_attachment'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<CertView>>
  }

  /**
   * ## 获取未审核证件数量
   * > 根据管理员查询条件统计各类证件的未审核数量
   *
   * 该接口用于管理员查看系统中各类证件的待审核数量统计，支持按证件类型、提交时间等条件筛选。
   *
   * @parameter {CertV2ApiOptions['getNotAuditCertCounts']} options
   * - spec 证件管理查询条件，包含证件类型、时间范围等过滤条件
   * @return 以证件类型ID为key，未审核数量为value的映射表
   */
  readonly getNotAuditCertCounts: (options: CertV2ApiOptions['getNotAuditCertCounts']) => Promise<
    { [key: string]: number }
  > = async (options) => {
    let _uri = '/v2/cert/not_audit_cert_counts'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.visible
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'visible='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.hasAuditStatus
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasAuditStatus='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.hasUserAccountIds
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasUserAccountIds='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.hasUserInfoIds
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasUserInfoIds='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<{ [key: string]: number }>
  }

  /**
   * ## 获取未审核的证件信息列表
   *
   * 此接口供管理员使用，用于获取符合特定条件的未审核证件信息列表 必须具备 ADMIN 权限才能调用此接口
   *
   * @parameter {CertV2ApiOptions['getNotAuditCertsAsAdmin']} options
   * - spec 用于指定查询条件的对象，包括用户信息 ID、用户账户 ID、可见性等
   * @return 返回一个包含未审核证件视图的分页对象
   *
   * 注意：调用此接口时，必须提供用户信息 ID 或用户账户 ID，否则将抛出异常
   */
  readonly getNotAuditCertsAsAdmin: (options: CertV2ApiOptions['getNotAuditCertsAsAdmin']) => Promise<
    IPage<CertView>
  > = async (options) => {
    let _uri = '/v2/cert/not_audit_certs'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.visible
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'visible='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.hasAuditStatus
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasAuditStatus='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.hasUserAccountIds
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasUserAccountIds='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.hasUserInfoIds
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'hasUserInfoIds='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<CertView>>
  }

  /**
   * ## 获取当前用户的标题图片附件
   * > 该接口用于获取当前登录用户的标题图片附件信息
   *
   * 通过用户ID查询可见的标题图片附件，返回认证视图列表
   * @parameter {CertV2ApiOptions['getTitleImageCertAsMe']} options
   * - authInfo 认证请求信息（自动注入，无需手动传递）
   * @return 认证视图列表，包含标题图片附件信息
   */
  readonly getTitleImageCertAsMe: () => Promise<
    Array<CertView>
  > = async () => {
    const _uri = '/v2/cert/me/title_image_attachment'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<CertView>>
  }

  /**
   * ## 根据 userInfoId 查询对应的水印证件文件
   * > 根据用户信息 id 查询对应的水印证件文件
   *
   * 该接口用于根据用户信息 id 查询对应的水印证件文件，返回认证视图列表
   *
   * @parameter {CertV2ApiOptions['getUserInfoWatermarkCerts']} options
   * - userInfoId 用户信息 id
   * @return 水印证件文件集合
   */
  readonly getUserInfoWatermarkCerts: (options: CertV2ApiOptions['getUserInfoWatermarkCerts']) => Promise<
    Array<CertView>
  > = async (options) => {
    let _uri = '/v2/cert/user_info_watermark_certs'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.userInfoId
    _uri += _separator
    _uri += 'userInfoId='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<CertView>>
  }

  /**
   * ## 根据 id 批量查询对应的水印证件文件
   *
   * @parameter {CertV2ApiOptions['getWatermarkAttachmentsGroupByIds']} options
   * - ids 证件 id 集合
   * @return 水印证件文件集合 id to 水印证件文件
   */
  readonly getWatermarkAttachmentsGroupByIds: (options: CertV2ApiOptions['getWatermarkAttachmentsGroupByIds']) => Promise<
    { [key: string]: Dynamic_Cert }
  > = async (options) => {
    let _uri = '/v2/cert/watermark_attachments_id_map'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.ids
    for (const _item of _value) {
      _uri += _separator
      _uri += 'ids='
      _uri += encodeURIComponent(_item)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<{ [key: string]: Dynamic_Cert }>
  }

  /**
   * 对现有的证件序列标记，进行压缩收紧处理
   */
  readonly patchCertTypingMarkersAsAdmin: (options: CertV2ApiOptions['patchCertTypingMarkersAsAdmin']) => Promise<
    Array<Dynamic_Cert>
  > = async (options) => {
    let _uri = '/v2/cert/compress_cert_marker'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.userInfoId
    _uri += _separator
    _uri += 'userInfoId='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    _value = options.userAccountId
    _uri += _separator
    _uri += 'userAccountId='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'PATCH' })) as Promise<Array<Dynamic_Cert>>
  }

  /**
   * ## 批量更新证件审核状态（管理员权限）
   * > 管理员批量更新证件的审核状态，将新的审核状态持久化到数据库中。
   *
   * 该接口用于管理员批量更新证件的审核状态，需要管理员权限才能调用。 接收一个包含证件审核状态信息的列表，调用 `certService` 的 `persistAuditStatus` 方法将其保存到数据库中。 最终返回保存后的证件实体列表。
   *
   * @parameter {CertV2ApiOptions['patchCertsAuditStatusesAsAdmin']} options
   * - authInfo 认证请求信息，包含当前用户的ID等信息
   * - auditList 包含证件审核状态信息的列表，每个元素为 `CertAdminPutAuditStatusDto` 类型
   * @return 返回保存后的证件实体列表，类型为 `List<Cert>`
   */
  readonly patchCertsAuditStatusesAsAdmin: (options: CertV2ApiOptions['patchCertsAuditStatusesAsAdmin']) => Promise<
    Array<Dynamic_Cert>
  > = async (options) => {
    const _uri = '/v2/cert/audit_statuses'
    return (await this.executor({ uri: _uri, method: 'PATCH', body: options.body })) as Promise<Array<Dynamic_Cert>>
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
   * @parameter {CertV2ApiOptions['postBankCardAsMe']} options
   * - authInfo 用户认证信息（自动注入，无需前端传递）
   * - dto 银行卡信息DTO，包含银行卡号、手机号、银行类型等
   * - head 银行卡正面照片（MultipartFile）
   * - tail 银行卡反面照片（MultipartFile，可选）
   * @return 保存后的银行卡信息实体
   */
  readonly postBankCardAsMe: (options: CertV2ApiOptions['postBankCardAsMe']) => Promise<
    Dynamic_BankCard
  > = async (options) => {
    const _uri = '/v2/cert/me/bank_card_attachment'
    const _formData = new FormData()
    const _body = options.body
    _formData.append(
      'dto',
      new Blob(
        [JSON.stringify(_body.dto)],
        { type: 'application/json' },
      ),
    )
    _formData.append('head', _body.head)
    if (_body.tail) {
      _formData.append('tail', _body.tail)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Dynamic_BankCard>
  }

  /**
   * ## 批量上传证件信息（管理员权限）
   * > 管理员批量上传证件信息，并关联文件附件
   *
   * 该接口用于管理员批量上传证件信息，每个证件信息可以关联多个文件附件。 需要管理员权限才能调用。
   *
   * @parameter {CertV2ApiOptions['postCertsAsAdmin']} options
   * - certs 证件信息列表，包含证件的基本信息
   * - files 文件附件列表，每个文件对应一个证件信息
   * - authRequestInfo 认证请求信息，包含当前用户的ID等信息
   * @return 返回上传成功的证件信息列表
   */
  readonly postCertsAsAdmin: (options: CertV2ApiOptions['postCertsAsAdmin']) => Promise<
    Array<Dynamic_Cert>
  > = async (options) => {
    const _uri = '/v2/cert/batch_certs'
    const _formData = new FormData()
    const _body = options.body
    _formData.append(
      'certs',
      new Blob(
        [JSON.stringify(_body.certs)],
        { type: 'application/json' },
      ),
    )
    for (const file of _body.files) {
      _formData.append('files', file)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Array<Dynamic_Cert>>
  }

  /**
   * ## 上传自身的残疾证
   *
   * @parameter {CertV2ApiOptions['postDisCard2AttachmentAsMe']} options
   * - head 残疾证正面或双面
   * - tail 残疾证反面
   */
  readonly postDisCard2AttachmentAsMe: (options: CertV2ApiOptions['postDisCard2AttachmentAsMe']) => Promise<
    Array<CertView>
  > = async (options) => {
    const _uri = '/v2/cert/me/dis_card_2_attachment'
    const _formData = new FormData()
    const _body = options.body
    _formData.append(
      'disInfoDto',
      new Blob(
        [JSON.stringify(_body.disInfoDto)],
        { type: 'application/json' },
      ),
    )
    _formData.append(
      'userInfoBase',
      new Blob(
        [JSON.stringify(_body.userInfoBase)],
        { type: 'application/json' },
      ),
    )
    _formData.append('head', _body.head)
    if (_body.tail) {
      _formData.append('tail', _body.tail)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Array<CertView>>
  }

  /**
   * ## 上传用户户口本附件
   * > 用于当前登录用户上传户口本附件信息
   *
   * 该接口支持上传户口本首页和尾页，尾页为可选参数。上传成功后返回证件视图信息。
   *
   * @parameter {CertV2ApiOptions['postHouseholdCardAttachmentAsMe']} options
   * - authInfo 认证信息（自动注入，API文档忽略）
   * - head 户口本首页（必传）
   * - tail 户口本尾页（可选）
   * @return 证件视图信息列表
   */
  readonly postHouseholdCardAttachmentAsMe: (options: CertV2ApiOptions['postHouseholdCardAttachmentAsMe']) => Promise<
    Array<CertView>
  > = async (options) => {
    const _uri = '/v2/cert/me/household_card_attachment'
    const _formData = new FormData()
    const _body = options.body
    _formData.append('head', _body.head)
    if (_body.tail) {
      _formData.append('tail', _body.tail)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Array<CertView>>
  }

  /**
   * ## 上传个人的身份证
   * > 此接口用于上传用户的二代身份证图片，并验证身份证号是否符合相关规则。
   * - 验证 `userInfoBase.idCard` 是否为空，若为空则抛出异常。
   * - 确保身份证号长度为 18 位，否则抛出异常。
   * - 调用 `userAccountService.checkIdCardCertCodeInDisCertCodeOrThrow` 方法检查身份证号是否存在于残疾证号中。
   * - 最后调用 `certService.postIdCard2Attachment` 方法完成身份证附件的上传。
   *
   * @parameter {CertV2ApiOptions['postIdCard2CertAsMe']} options
   * - authInfo 用户认证信息，包含用户身份信息。此参数由系统自动注入。
   * - userInfoBase 包含用户基本信息的对象，必须包含有效的身份证号。
   * - head 身份证正面图片文件。
   * - tail 身份证反面图片文件（可选）。
   * @return 返回一个包含上传成功的身份证视图对象列表。
   */
  readonly postIdCard2CertAsMe: (options: CertV2ApiOptions['postIdCard2CertAsMe']) => Promise<
    Array<CertView>
  > = async (options) => {
    const _uri = '/v2/cert/me/id_card_2_attachment'
    const _formData = new FormData()
    const _body = options.body
    _formData.append(
      'userInfoBase',
      new Blob(
        [JSON.stringify(_body.userInfoBase)],
        { type: 'application/json' },
      ),
    )
    _formData.append('head', _body.head)
    if (_body.tail) {
      _formData.append('tail', _body.tail)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Array<CertView>>
  }

  /**
   * ## 上传自身寸照
   */
  readonly postTitleImageCertAsMe: (options: CertV2ApiOptions['postTitleImageCertAsMe']) => Promise<
    Array<CertView>
  > = async (options) => {
    const _uri = '/v2/cert/me/title_image_attachment'
    const _formData = new FormData()
    const _body = options.body
    _formData.append('head', _body.head)
    if (_body.tail) {
      _formData.append('tail', _body.tail)
    }
    return (await this.executor({ uri: _uri, method: 'POST', body: _formData })) as Promise<Array<CertView>>
  }
}

export interface CertV2ApiOptions {
  patchCertTypingMarkersAsAdmin: {
    userInfoId: number
    userAccountId: number
  }
  patchCertsAuditStatusesAsAdmin: {
    /**
     * 包含证件审核状态信息的列表，每个元素为 `CertAdminPutAuditStatusDto` 类型
     */
    body: Array<CertAdminPutAuditStatusDto>
  }
  getNotAuditCertsAsAdmin: {
    /**
     * 用于指定查询条件的对象，包括用户信息 ID、用户账户 ID、可见性等
     */
    spec: CertAdminSpec
  }
  getBanks: {}
  deleteBankCardAttachmentAsMe: {
    /**
     * 银行卡ID，用于指定要删除的银行卡附件
     */
    bankCardId: number
  }
  getNotAuditCertCounts: {
    /**
     * 证件管理查询条件，包含证件类型、时间范围等过滤条件
     */
    spec: CertAdminSpec
  }
  getBankCardsAsMe: {}
  postBankCardAsMe: {
    body: {
      dto: BankCardDto
      head: File
      tail?: File | undefined
    }
  }
  postHouseholdCardAttachmentAsMe: {
    body: {
      head: File
      tail?: File | undefined
    }
  }
  getHouseHoldCardAttachmentAsMe: {}
  deleteIdCard2AttachmentAsMe: {}
  getIdCard2AttachmentAsMe: {}
  postIdCard2CertAsMe: {
    body: {
      userInfoBase: UserInfoPutDto
      head: File
      tail?: File | undefined
    }
  }
  deleteTitleImageCertAsMe: {}
  getTitleImageCertAsMe: {}
  postTitleImageCertAsMe: {
    body: {
      head: File
      tail?: File | undefined
    }
  }
  getDisCard2AttachmentAsMe: {}
  postDisCard2AttachmentAsMe: {
    body: {
      disInfoDto: DisInfoPostMeDto
      userInfoBase: UserInfoPutDto
      head: File
      tail?: File | undefined
    }
  }
  deleteHouseholdAttachmentByUserAccountId: {}
  deleteDisCard2AttachmentAsMe: {}
  getWatermarkAttachmentsGroupByIds: {
    /**
     * 证件 id 集合
     */
    ids: Array<number>
  }
  getUserInfoWatermarkCerts: {
    /**
     * 用户信息 id
     */
    userInfoId: number
  }
  postCertsAsAdmin: {
    body: {
      certs: Array<CertAdminPostDto>
      files: Array<File>
    }
  }
}
