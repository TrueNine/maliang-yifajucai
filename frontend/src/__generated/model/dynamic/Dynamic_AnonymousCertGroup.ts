import type { Dynamic_Cert, Dynamic_UserAccount } from './'

/**
 * # 匿名证件组
 */
export interface Dynamic_AnonymousCertGroup {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  /**
   * 上传证件 id
   */
  certId?: number
  /**
   * 上传证件
   */
  cert?: Dynamic_Cert
  /**
   * 上传账号 id
   */
  upUserId?: number
  /**
   * 上传账号
   */
  uploadUserAccount?: Dynamic_UserAccount
  /**
   * 证件序列号
   */
  serialNo?: string
}
