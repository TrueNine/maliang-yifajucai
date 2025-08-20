import type { AuditTyping } from '../enums/'

/**
 * 用户证件
 */
export interface CertAdminPutAuditStatusDto {
  id?: string | undefined
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined
  /**
   * 对于用户是否可见
   */
  visible?: boolean | undefined
}
