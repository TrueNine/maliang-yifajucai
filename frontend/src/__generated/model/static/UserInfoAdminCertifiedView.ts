import type {GenderTyping} from '../enums/';
import type {UserInfoAdminCertifiedView_TargetOf_account, UserInfoAdminCertifiedView_TargetOf_address, UserInfoAdminCertifiedView_TargetOf_disInfo} from './';

/**
 * # 自然人用户信息
 */
export interface UserInfoAdminCertifiedView {
  id: string;
  /**
   * 用户账号主体
   */
  account?: UserInfoAdminCertifiedView_TargetOf_account | undefined;
  metadataName?: string | undefined;
  metadataIdCard?: string | undefined;
  metadataPhone?: string | undefined;
  /**
   * 性别
   */
  gender?: GenderTyping | undefined;
  /**
   * 所关联地址
   */
  address?: UserInfoAdminCertifiedView_TargetOf_address | undefined;
  /**
   * 残疾信息
   */
  disInfo?: UserInfoAdminCertifiedView_TargetOf_disInfo | undefined;
}
