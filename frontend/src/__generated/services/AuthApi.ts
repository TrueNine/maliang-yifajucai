import type {Executor} from '../';
import type {Dynamic_UserAccount} from '../model/dynamic/';
import type {
  AuthApi_AccountDto, 
  AuthService_AuthTokenView, 
  IPage, 
  UserAccountAdminSpec, 
  UserAccountAdminView
} from '../model/static/';

/**
 * # 用户认证授权 API
 * 
 * 该 API 注重于认证授权，但不涉及直接权限管理
 * - 账号登录登出
 * - 下发 token
 * - 第三方平台登录
 * - 踢人
 * 
 */
export class AuthApi {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 获取当前登录用户信息
   */
  readonly getCurrentUserAccount: () => Promise<
    Dynamic_UserAccount | undefined
  > = async() => {
    let _uri = '/v2/auth/me/auth_request_info';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<Dynamic_UserAccount | undefined>;
  }
  
  /**
   * ## 批量查询账号信息
   */
  readonly getUserAccountsAsAdmin: (options: AuthApiOptions['getUserAccountsAsAdmin']) => Promise<
    IPage<UserAccountAdminView>
  > = async(options) => {
    let _uri = '/v2/auth/user_accounts';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.spec.id;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.account;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'account='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.nickName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'nickName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.createUserAccountId;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'createUserAccountId='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.createUserAccountAccount;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'createUserAccountAccount='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    _value = options.spec.createUserAccountNickName;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'createUserAccountNickName='
      _uri += encodeURIComponent(_value);
      _separator = '&';
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
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<IPage<UserAccountAdminView>>;
  }
  
  /**
   * ## 批量查询账号信息
   * TODO 管理员
   * 
   * @parameter {AuthApiOptions['getUserAccountsByIdsAsAdmin']} options
   * - ids 批量用户 id
   */
  readonly getUserAccountsByIdsAsAdmin: (options: AuthApiOptions['getUserAccountsByIdsAsAdmin']) => Promise<
    Array<Dynamic_UserAccount>
  > = async(options) => {
    let _uri = '/v2/auth/user_accounts/ids';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.ids;
    for (const _item of _value) {
      _uri += _separator
      _uri += 'ids='
      _uri += encodeURIComponent(_item);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<Array<Dynamic_UserAccount>>;
  }
  
  /**
   * ## 账号密码登录
   * 
   * @parameter {AuthApiOptions['loginBySystemAccount']} options
   * - dto 账号密码，密码以 base64 编码进行传递
   */
  readonly loginBySystemAccount: (options: AuthApiOptions['loginBySystemAccount']) => Promise<
    AuthService_AuthTokenView
  > = async(options) => {
    let _uri = '/v2/auth/login/account';
    return (await this.executor({uri: _uri, method: 'POST', body: options.body})) as Promise<AuthService_AuthTokenView>;
  }
  
  /**
   * ## 微信公众号 code 一键登录
   * > TODO 接口防抖，防止频繁重复登录
   * 
   * @parameter {AuthApiOptions['loginByWxpaCode']} options
   * - code 微信公众号 code
   */
  readonly loginByWxpaCode: (options: AuthApiOptions['loginByWxpaCode']) => Promise<
    AuthService_AuthTokenView | undefined
  > = async(options) => {
    let _uri = '/v2/auth/login/wxpa_fast_login_code';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.code;
    _uri += _separator
    _uri += 'code='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'POST'})) as Promise<AuthService_AuthTokenView | undefined>;
  }
  
  /**
   * ## 当前 session 退出登录
   */
  readonly logoutByAccount: () => Promise<
    void
  > = async() => {
    let _uri = '/v2/auth/logout';
    return (await this.executor({uri: _uri, method: 'POST'})) as Promise<void>;
  }
  
  /**
   * ## 修改自身密码
   * 
   * @parameter {AuthApiOptions['patchMePassword']} options
   * - oldPassword 旧密码
   * - newPassword 新密码
   * - newPasswordConfirm 新密码确认
   */
  readonly patchMePassword: (options: AuthApiOptions['patchMePassword']) => Promise<
    Dynamic_UserAccount
  > = async(options) => {
    let _uri = '/v2/auth/me/password';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.oldPassword;
    _uri += _separator
    _uri += 'oldPassword='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.newPassword;
    _uri += _separator
    _uri += 'newPassword='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.newPasswordConfirm;
    _uri += _separator
    _uri += 'newPasswordConfirm='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'PATCH'})) as Promise<Dynamic_UserAccount>;
  }
}

export type AuthApiOptions = {
  'patchMePassword': {
    /**
     * 旧密码
     */
    oldPassword: string, 
    /**
     * 新密码
     */
    newPassword: string, 
    /**
     * 新密码确认
     */
    newPasswordConfirm: string
  }, 
  'loginByWxpaCode': {
    /**
     * 微信公众号 code
     */
    code: string
  }, 
  'getUserAccountsByIdsAsAdmin': {
    /**
     * 批量用户 id
     */
    ids: Array<number>
  }, 
  'getCurrentUserAccount': {}, 
  'loginBySystemAccount': {
    /**
     * 账号密码，密码以 base64 编码进行传递
     */
    body: AuthApi_AccountDto
  }, 
  'logoutByAccount': {}, 
  'getUserAccountsAsAdmin': {
    spec: UserAccountAdminSpec
  }
}
