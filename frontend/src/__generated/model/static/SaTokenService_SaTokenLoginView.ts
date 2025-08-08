/**
 * # 当前登录状态
 * 
 */
export interface SaTokenService_SaTokenLoginView {
  getHeaderName: string;
  token?: string | undefined;
  login?: boolean | undefined;
  tokenTimeout?: string | undefined;
  activeTimeout?: string | undefined;
  roles: Array<string>;
  permissions: Array<string>;
  account: string;
}
