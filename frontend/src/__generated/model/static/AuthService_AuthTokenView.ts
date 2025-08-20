/**
 * # 当前登录状态
 * 
 */
export interface AuthService_AuthTokenView {
  sessionId?: string | undefined;
  login: boolean;
  sessionTimeout?: string | undefined;
  roles: Array<string>;
  permissions: Array<string>;
  account: string;
  userId: number;
}
