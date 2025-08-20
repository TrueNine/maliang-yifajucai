import type { AuditTyping } from '../enums/'

/**
 * 职位
 */
export interface JobAdminSpec {
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined
  o?: number | undefined
  s?: number | undefined
}
