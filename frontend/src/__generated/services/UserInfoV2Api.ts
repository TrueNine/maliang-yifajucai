import type {Executor} from '../';
import type {Dynamic_DisInfo, Dynamic_UserInfo} from '../model/dynamic/';
import type {
  CertAdminCertifiedUserInfoSpec, 
  IPage, 
  UserInfoAdminCertifiedView, 
  UserInfoAdminPostDto, 
  UserInfoAdminPutDto, 
  UserInfoAdminSpec, 
  UserInfoAdminView, 
  UserInfoMemberView, 
  UserInfoPutDto
} from '../model/static/';

/**
 * # 用户信息第二版接口
 * 
 */
export class UserInfoV2Api {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 获取认证用户信息视图（管理员权限）
   * > 管理员权限下获取认证用户信息的视图
   * 
   * 该接口用于管理员权限下获取认证用户信息的视图，需要具备 `ADMIN` 权限。
   * 
   * @parameter {UserInfoV2ApiOptions['getCertifiedUserInfoViewAsAdmin']} options
   * - spec 查询参数，包含获取认证用户信息所需的条件
   * @return 认证用户信息的视图，如果未找到则返回 `null`
   */
  readonly getCertifiedUserInfoViewAsAdmin: (options: UserInfoV2ApiOptions['getCertifiedUserInfoViewAsAdmin']) => Promise<
    UserInfoAdminCertifiedView | undefined
  > = async(options) => {
    let _uri = '/v2/user_info/certified_user_info';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.doType;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doType='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.groupCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'groupCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.userAccountIdOrUserInfoId;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'userAccountIdOrUserInfoId='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<UserInfoAdminCertifiedView | undefined>;
  }
  
  /**
   * ## 获取用户实名认证状态
   * > 查询当前登录用户的实名认证状态
   * 
   * 该接口用于获取当前登录用户是否已完成实名认证。需要用户已登录状态。
   * 
   * @parameter {UserInfoV2ApiOptions['getCitizenVerified']} options
   * - authInfo 用户认证信息（自动注入，无需手动传递）
   * @return 返回用户实名认证状态（true：已认证，false：未认证）
   */
  readonly getCitizenVerified: () => Promise<
    boolean
  > = async() => {
    let _uri = '/v2/user_info/me/citizen_verified';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<boolean>;
  }
  
  /**
   * ## 获取当前用户的残疾信息
   * > 通过认证信息获取当前用户的残疾信息
   * 
   * 该接口用于获取当前登录用户的残疾信息，需要用户已登录。
   * 
   * @parameter {UserInfoV2ApiOptions['getDisInfoAsMe']} options
   * - authInfo 认证信息，包含用户ID
   * @return 当前用户的残疾信息，如果不存在则返回null
   */
  readonly getDisInfoAsMe: () => Promise<
    Dynamic_DisInfo | undefined
  > = async() => {
    let _uri = '/v2/user_info/me/dis_info';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<Dynamic_DisInfo | undefined>;
  }
  
  /**
   * ## 查询各省份的用户数量
   * 
   * @parameter {UserInfoV2ApiOptions['getUserCountByProvinceAsAdmin']} options
   * - spec 查询条件
   * @return 省份和用户数量
   */
  readonly getUserCountByProvinceAsAdmin: (options: UserInfoV2ApiOptions['getUserCountByProvinceAsAdmin']) => Promise<
    {[key:string]: number}
  > = async(options) => {
    let _uri = '/v2/user_info/province_user_info_count';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.isBlacked;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'isBlacked='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.id;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.minAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'minAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.maxAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'maxAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.addressCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'addressCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.name;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.firstName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'firstName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.lastName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'lastName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.phone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'phone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.sparePhone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'sparePhone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.idCard;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'idCard='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.disInfoTypeIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoTypeIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoLevelIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoLevelIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoCertCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'disInfoCertCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.certsBankGroupValueIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'certsBankGroupValueIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.o;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.s;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<{[key:string]: number}>;
  }
  
  /**
   * ## 根据当前登录用户，查询当前用户信息
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfoAsMe']} options
   * - authInfo 当前登录用户信息
   * @return 当前登录用户信息
   */
  readonly getUserInfoAsMe: () => Promise<
    UserInfoMemberView | undefined
  > = async() => {
    let _uri = '/v2/user_info/me';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<UserInfoMemberView | undefined>;
  }
  
  /**
   * ## 根据用户ID获取用户信息（管理员权限）
   * > 通过用户ID查询用户信息，仅限管理员权限访问。
   * 
   * 该接口用于管理员根据用户ID查询用户信息。如果用户ID为空，则返回null。
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfoByIdAsAdmin']} options
   * - id 用户ID，类型为RefId
   * @return 返回用户信息视图，类型为UserInfoAdminView?，如果用户ID为空或未找到对应信息，则返回null
   */
  readonly getUserInfoByIdAsAdmin: (options: UserInfoV2ApiOptions['getUserInfoByIdAsAdmin']) => Promise<
    UserInfoAdminView | undefined
  > = async(options) => {
    let _uri = '/v2/user_info/id/';
    _uri += encodeURIComponent(options.id);
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<UserInfoAdminView | undefined>;
  }
  
  /**
   * ## 获取用户信息详情（管理员权限）
   * > 通过用户信息ID查询用户信息详情，仅限管理员权限访问。
   * 
   * 该接口用于管理员根据用户信息ID查询用户信息详情。如果用户信息ID为空，则返回null。
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfoDetailsByUserInfoIdAsAdmin']} options
   * - userInfoId 用户信息ID，类型为RefId
   * @return 返回用户信息视图，类型为UserInfoAdminView?，如果用户信息ID为空或未找到对应信息，则返回null
   */
  readonly getUserInfoDetailsByUserInfoIdAsAdmin: (options: UserInfoV2ApiOptions['getUserInfoDetailsByUserInfoIdAsAdmin']) => Promise<
    UserInfoAdminView | undefined
  > = async(options) => {
    let _uri = '/v2/user_info/user_info_details';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.userInfoId;
    _uri += _separator
    _uri += 'userInfoId='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<UserInfoAdminView | undefined>;
  }
  
  /**
   * ## 获取当前用户的个人信息表单
   * > 通过认证信息获取当前用户的个人信息表单
   * 
   * 该接口用于获取当前登录用户的个人信息表单数据。需要用户已登录才能访问。
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfoPutDtoAsMe']} options
   * - authInfo 认证信息，包含当前登录用户的ID
   * @return 当前用户的个人信息表单数据，如果用户不存在则返回null
   */
  readonly getUserInfoPutDtoAsMe: () => Promise<
    Dynamic_UserInfo | undefined
  > = async() => {
    let _uri = '/v2/user_info/me/form';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<Dynamic_UserInfo | undefined>;
  }
  
  /**
   * ## 获取用户信息表单（管理员权限）
   * > 管理员权限下获取用户信息表单数据
   * 
   * 该接口用于管理员权限下获取用户信息表单数据，需要用户登录且具有 `ADMIN` 权限。
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfoPutFormAsAdmin']} options
   * - spec 用户信息查询条件
   * @return 用户信息表单数据，若不存在则返回 `null`
   */
  readonly getUserInfoPutFormAsAdmin: (options: UserInfoV2ApiOptions['getUserInfoPutFormAsAdmin']) => Promise<
    UserInfoAdminPostDto | undefined
  > = async(options) => {
    let _uri = '/v2/user_info/form';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.isBlacked;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'isBlacked='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.id;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.minAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'minAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.maxAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'maxAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.addressCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'addressCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.name;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.firstName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'firstName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.lastName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'lastName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.phone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'phone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.sparePhone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'sparePhone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.idCard;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'idCard='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.disInfoTypeIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoTypeIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoLevelIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoLevelIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoCertCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'disInfoCertCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.certsBankGroupValueIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'certsBankGroupValueIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.o;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.s;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<UserInfoAdminPostDto | undefined>;
  }
  
  /**
   * ## 获取用户信息列表（管理员权限）
   * > 管理员权限下获取用户信息列表，支持分页和条件查询
   * 
   * 根据传入的查询条件和分页参数，返回符合条件的分页用户信息列表。
   * 
   * @parameter {UserInfoV2ApiOptions['getUserInfosAsAdmin']} options
   * - spec 用户信息查询条件，可为空
   * @return 分页后的用户信息列表
   */
  readonly getUserInfosAsAdmin: (options: UserInfoV2ApiOptions['getUserInfosAsAdmin']) => Promise<
    IPage<UserInfoAdminView>
  > = async(options) => {
    let _uri = '/v2/user_info/user_infos';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.isBlacked;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'isBlacked='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.id;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.minAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'minAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.maxAge;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'maxAge='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.addressCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'addressCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.name;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.firstName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'firstName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.lastName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'lastName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.phone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'phone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.sparePhone;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'sparePhone='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.idCard;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'idCard='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.disInfoTypeIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoTypeIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoLevelIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'disInfoLevelIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.disInfoCertCode;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'disInfoCertCode='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.certsBankGroupValueIn;
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'certsBankGroupValueIn='
        _uri += encodeURIComponent(_item);
        _separator = '&';
      }
    }
    _value = options.spec.o;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.s;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<IPage<UserInfoAdminView>>;
  }
  
  /**
   * ## 管理端保存用户信息
   * > 管理员权限下保存用户信息
   * 
   * 该接口用于管理员权限下保存用户信息，需要具备 `ADMIN` 权限。
   * 
   * @parameter {UserInfoV2ApiOptions['postUserInfoAsAdmin']} options
   * - dto 用户信息数据传输对象，包含需要保存的用户信息
   * @return 保存后的用户信息
   */
  readonly postUserInfoAsAdmin: (options: UserInfoV2ApiOptions['postUserInfoAsAdmin']) => Promise<
    Dynamic_UserInfo
  > = async(options) => {
    let _uri = '/v2/user_info/';
    return (await this.executor({uri: _uri, method: 'POST', body: options.body})) as Promise<Dynamic_UserInfo>;
  }
  
  /**
   * ## 管理端保存用户信息
   * > 管理员权限下保存用户信息
   * 
   * 该接口用于管理员权限下保存用户信息，需要具备 `ADMIN` 权限。
   * 
   * @parameter {UserInfoV2ApiOptions['putUserInfoAsAdmin']} options
   * - dto 用户信息数据传输对象，包含需要保存的用户信息
   * @return 保存后的用户信息
   */
  readonly putUserInfoAsAdmin: (options: UserInfoV2ApiOptions['putUserInfoAsAdmin']) => Promise<
    Dynamic_UserInfo
  > = async(options) => {
    let _uri = '/v2/user_info/';
    return (await this.executor({uri: _uri, method: 'PUT', body: options.body})) as Promise<Dynamic_UserInfo>;
  }
  
  /**
   * ## 修改当前自身登录的用户信息
   * 
   * @parameter {UserInfoV2ApiOptions['putUserInfoAsMe']} options
   * - modifyUserInfo 修改的用户信息
   * @return 修改后的用户信息
   */
  readonly putUserInfoAsMe: (options: UserInfoV2ApiOptions['putUserInfoAsMe']) => Promise<
    Dynamic_UserInfo
  > = async(options) => {
    let _uri = '/v2/user_info/me';
    return (await this.executor({uri: _uri, method: 'PUT', body: options.body})) as Promise<Dynamic_UserInfo>;
  }
}

export type UserInfoV2ApiOptions = {
  'getDisInfoAsMe': {}, 
  'getCertifiedUserInfoViewAsAdmin': {
    /**
     * 查询参数，包含获取认证用户信息所需的条件
     */
    spec: CertAdminCertifiedUserInfoSpec
  }, 
  'getCitizenVerified': {}, 
  'putUserInfoAsMe': {
    /**
     * 修改的用户信息
     */
    body: UserInfoPutDto
  }, 
  'getUserInfoAsMe': {}, 
  'postUserInfoAsAdmin': {
    /**
     * 用户信息数据传输对象，包含需要保存的用户信息
     */
    body: UserInfoAdminPostDto
  }, 
  'getUserInfoByIdAsAdmin': {
    /**
     * 用户ID，类型为RefId
     */
    id: number
  }, 
  'getUserInfoPutFormAsAdmin': {
    /**
     * 用户信息查询条件
     */
    spec: UserInfoAdminSpec
  }, 
  'putUserInfoAsAdmin': {
    /**
     * 用户信息数据传输对象，包含需要保存的用户信息
     */
    body: UserInfoAdminPutDto
  }, 
  'getUserInfoPutDtoAsMe': {}, 
  'getUserInfosAsAdmin': {
    /**
     * 用户信息查询条件，可为空
     */
    spec: UserInfoAdminSpec
  }, 
  'getUserInfoDetailsByUserInfoIdAsAdmin': {
    /**
     * 用户信息ID，类型为RefId
     */
    userInfoId: number
  }, 
  'getUserCountByProvinceAsAdmin': {
    /**
     * 查询条件
     */
    spec: UserInfoAdminSpec
  }
}
