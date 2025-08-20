import type {BlackListAdminView_TargetOf_blackUserInfo_TargetOf_account} from './';

/**
 * # 自然人用户信息
 */
export interface BlackListAdminView_TargetOf_blackUserInfo {
  metadataName?: string | undefined;
  metadataPhone?: string | undefined;
  metadataIdCard?: string | undefined;
  /**
   * 用户账号主体
   */
  account?: BlackListAdminView_TargetOf_blackUserInfo_TargetOf_account | undefined;
}
