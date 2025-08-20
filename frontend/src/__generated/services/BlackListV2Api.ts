import type {Executor} from '../';
import type {Dynamic_BlackList} from '../model/dynamic/';
import type {AuditTyping} from '../model/enums/';
import type {
  BlackListAdminPostDto, 
  BlackListAdminView, 
  BlackListEventDocSpec, 
  BlackListEventDocView, 
  IPage, 
  IPageParam
} from '../model/static/';

/**
 * # 黑名单管理 API V2
 * > 提供黑名单的增删改查功能
 * 
 * 该控制器负责处理与黑名单相关的所有请求，包括添加、删除、查询等操作。
 * 使用 V2 版本的 API 路径，确保与旧版本兼容。
 * 
 */
export class BlackListV2Api {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 获取黑名单列表（管理员视图）
   * > 该接口仅限管理员权限访问，返回黑名单的管理员视图数据
   * 
   * 通过分页查询参数获取黑名单列表，并使用 `BlackListAdminView` 视图进行数据转换
   * @parameter {BlackListV2ApiOptions['getBlackListsAsAdmin']} options
   * - pq 分页查询参数，默认值为 `Pq.DEFAULT_MAX`
   * @return 包含黑名单管理员视图的分页结果 `Pr<BlackListAdminView>`
   */
  readonly getBlackListsAsAdmin: (options: BlackListV2ApiOptions['getBlackListsAsAdmin']) => Promise<
    IPage<BlackListAdminView>
  > = async(options) => {
    let _uri = '/v2/black_list/admin';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.pq.s;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.pq.o;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.pq.u;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'u='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<IPage<BlackListAdminView>>;
  }
  
  /**
   * ## 个人事件文档
   * @parameter {BlackListV2ApiOptions['getPersonalEventDoc']} options
   * - spec 查询条件，包含黑名单事件文档的过滤条件
   * @return 符合条件的黑名单事件文档视图，如果未找到则返回null
   */
  readonly getPersonalEventDoc: (options: BlackListV2ApiOptions['getPersonalEventDoc']) => Promise<
    BlackListEventDocView | undefined
  > = async(options) => {
    let _uri = '/v2/black_list/personal_event_doc';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.eventDoc;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'eventDoc='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.minOnDate;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'minOnDate='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.maxOnDate;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'maxOnDate='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoPhone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoPhone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoIdCard;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoIdCard='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoDisInfoCertCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoDisInfoCertCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoDisInfoDsType;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoDisInfoDsType='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.blackUserInfoDisInfoLevel;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'blackUserInfoDisInfoLevel='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<BlackListEventDocView | undefined>;
  }
  
  /**
   * ## 查询多个省份的统计信息
   * 
   * @parameter {BlackListV2ApiOptions['getProvincesStatistics']} options
   * - rankNum 统计数量 默认为 10
   * @return 省份代码，统计数量
   */
  readonly getProvincesStatistics: (options: BlackListV2ApiOptions['getProvincesStatistics']) => Promise<
    {[key:string]: number}
  > = async(options) => {
    let _uri = '/v2/black_list/statistics/provinces_rank';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.rankNum;
    _uri += _separator
    _uri += 'rankNum='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<{[key:string]: number}>;
  }
  
  /**
   * ## 更新黑名单审核状态
   * > 管理员权限下更新指定黑名单的审核状态
   * 
   * 该接口仅允许具有ADMIN权限的用户调用，用于更新黑名单的审核状态。
   * 更新操作通过Jimmer DSL实现，确保类型安全。
   * 
   * @parameter {BlackListV2ApiOptions['patchAuditStatusByIdAsAdmin']} options
   * - id 黑名单记录的唯一标识符，类型为RefId
   * - auditStatus 要更新的审核状态，类型为AuditTyping枚举
   * @return 更新后的黑名单实体对象，如果更新失败则返回null
   */
  readonly patchAuditStatusByIdAsAdmin: (options: BlackListV2ApiOptions['patchAuditStatusByIdAsAdmin']) => Promise<
    Dynamic_BlackList | undefined
  > = async(options) => {
    let _uri = '/v2/black_list/audit_status/';
    _uri += encodeURIComponent(options.id);
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.auditStatus;
    _uri += _separator
    _uri += 'auditStatus='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'PATCH'})) as Promise<Dynamic_BlackList | undefined>;
  }
  
  /**
   * ## 管理员添加黑名单
   * > 该接口仅限管理员权限使用，用于将用户加入黑名单
   * 
   * 该接口接收管理员提交的黑名单信息，并自动填充创建人、创建时间等系统字段。
   * 黑名单的审核状态初始化为`null`，需后续审核流程处理。
   * 
   * @parameter {BlackListV2ApiOptions['postBlackListAsAdmin']} options
   * - dto 黑名单信息DTO，包含黑名单相关字段
   * - auth 认证信息，用于获取当前操作人ID
   * @return 返回创建后的黑名单实体
   */
  readonly postBlackListAsAdmin: (options: BlackListV2ApiOptions['postBlackListAsAdmin']) => Promise<
    Dynamic_BlackList
  > = async(options) => {
    let _uri = '/v2/black_list/admin';
    return (await this.executor({uri: _uri, method: 'POST', body: options.body})) as Promise<Dynamic_BlackList>;
  }
}

export type BlackListV2ApiOptions = {
  'getProvincesStatistics': {
    /**
     * 统计数量 默认为 10
     */
    rankNum: number
  }, 
  'postBlackListAsAdmin': {
    /**
     * 黑名单信息DTO，包含黑名单相关字段
     */
    body: BlackListAdminPostDto
  }, 
  'getBlackListsAsAdmin': {
    /**
     * 分页查询参数，默认值为 `Pq.DEFAULT_MAX`
     */
    pq: IPageParam
  }, 
  'getPersonalEventDoc': {
    /**
     * 查询条件，包含黑名单事件文档的过滤条件
     */
    spec: BlackListEventDocSpec
  }, 
  'patchAuditStatusByIdAsAdmin': {
    /**
     * 黑名单记录的唯一标识符，类型为RefId
     */
    id: number, 
    /**
     * 要更新的审核状态，类型为AuditTyping枚举
     */
    auditStatus: AuditTyping
  }
}
