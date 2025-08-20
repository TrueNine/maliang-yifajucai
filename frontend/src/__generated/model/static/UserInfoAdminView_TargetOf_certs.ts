import type { CertContentTyping, CertPointTyping, CertTyping } from '../enums/'
import type { UserInfoAdminView_TargetOf_certs_TargetOf_createUserAccount, UserInfoAdminView_TargetOf_certs_TargetOf_waterMarkerAttachment } from './'

/**
 * 用户证件
 */
export interface UserInfoAdminView_TargetOf_certs {
  /**
   * 水印码
   *
   * water marker code
   */
  wmCode?: string | undefined
  /**
   * 证件内容类型
   */
  coType?: CertContentTyping | undefined
  /**
   * 证件印面类型
   */
  poType?: CertPointTyping | undefined
  /**
   * 证件类型
   */
  doType?: CertTyping | undefined
  /**
   * 创建此证件的账号
   */
  createUserAccount?: UserInfoAdminView_TargetOf_certs_TargetOf_createUserAccount | undefined
  createDatetime?: string | undefined
  /**
   * 水印图片
   */
  waterMarkerAttachment?: UserInfoAdminView_TargetOf_certs_TargetOf_waterMarkerAttachment | undefined
}
