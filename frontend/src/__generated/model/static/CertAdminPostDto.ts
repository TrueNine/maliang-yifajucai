import type { CertContentTyping, CertPointTyping, CertTyping } from '../enums/'

/**
 * 用户证件
 */
export interface CertAdminPostDto {
  /**
   * 证件类型
   */
  doType?: CertTyping | undefined
  /**
   * 证件内容类型
   */
  coType?: CertContentTyping | undefined
  /**
   * 证件印面类型
   */
  poType?: CertPointTyping | undefined
  userAccountId?: string | undefined
  userInfoId?: string | undefined
}
