import type {AuditTyping} from '../enums/';

/**
 * 用户证件
 */
export interface CertAdminSpec {
  /**
   * 对于用户是否可见
   */
  visible?: boolean | undefined;
  /**
   * 审核状态
   */
  hasAuditStatus?: Array<AuditTyping> | undefined;
  hasUserAccountIds?: Array<string> | undefined;
  hasUserInfoIds?: Array<string> | undefined;
  o?: number | undefined;
  s?: number | undefined;
}
