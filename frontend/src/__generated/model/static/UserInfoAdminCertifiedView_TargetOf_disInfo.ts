import type {DisTyping} from '../enums/';

/**
 * 残疾证信息
 */
export interface UserInfoAdminCertifiedView_TargetOf_disInfo {
  metadataCertCode?: string | undefined;
  /**
   * 残疾类别
   */
  dsType?: DisTyping | undefined;
  /**
   * 等级
   */
  level?: number | undefined;
}
