import type { CertTyping } from '../enums/'

/**
 * 用户证件
 */
export interface CertAdminCertifiedUserInfoSpec {
  /**
   * 证件类型
   */
  doType?: CertTyping | undefined
  /**
   * 证件组编号
   */
  groupCode?: string | undefined
  userAccountIdOrUserInfoId?: string | undefined
}
