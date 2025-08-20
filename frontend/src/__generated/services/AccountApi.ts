import type {Executor} from '../';
import type {Dynamic_UserAccount} from '../model/dynamic/';

/**
 * ## 账号管理 API 第 2 版
 * 
 * 该接口注重于直接的账号本身管理
 * 
 */
export class AccountApi {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 更新当前用户昵称
   * > 通过认证信息获取用户ID，更新对应用户的昵称
   * 
   * 该接口需要用户登录状态，使用PATCH方法更新用户昵称
   * 
   * @parameter {AccountApiOptions['patchNickNameAsMe']} options
   * - newNickName 新的昵称，通过请求参数传递
   * - authInfo 认证信息，包含当前用户ID，通过`@ApiIgnore`隐藏
   * @return 更新后的用户账户信息
   */
  readonly patchNickNameAsMe: (options: AccountApiOptions['patchNickNameAsMe']) => Promise<
    Dynamic_UserAccount
  > = async(options) => {
    let _uri = '/v2/account/me/nick_name';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.newNickName;
    _uri += _separator
    _uri += 'newNickName='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'PATCH'})) as Promise<Dynamic_UserAccount>;
  }
}

export type AccountApiOptions = {
  'patchNickNameAsMe': {
    /**
     * 新的昵称，通过请求参数传递
     */
    newNickName: string
  }
}
