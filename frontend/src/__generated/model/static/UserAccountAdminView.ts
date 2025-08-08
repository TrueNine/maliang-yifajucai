import type {UserAccountAdminView_TargetOf_createUserAccount} from './';

/**
 * # 用户账号
 */
export interface UserAccountAdminView {
  id: string;
  metadataAccount: string;
  nickName?: string | undefined;
  createUserAccount?: UserAccountAdminView_TargetOf_createUserAccount | undefined;
}
