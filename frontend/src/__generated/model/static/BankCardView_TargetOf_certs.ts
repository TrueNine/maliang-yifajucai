import type {
  AuditTyping,
  CertContentTyping,
  CertPointTyping,
  CertTyping,
} from '../enums/'
import type { BankCardView_TargetOf_certs_TargetOf_waterMarkerAttachment } from './'

/**
 * 用户证件
 */
export interface BankCardView_TargetOf_certs {
  id: string
  userInfoId?: string | undefined
  userId?: string | undefined
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
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined
  /**
   * 证件组编号
   */
  groupCode?: string | undefined
  /**
   * 水印图片
   */
  waterMarkerAttachment?: BankCardView_TargetOf_certs_TargetOf_waterMarkerAttachment | undefined
}
