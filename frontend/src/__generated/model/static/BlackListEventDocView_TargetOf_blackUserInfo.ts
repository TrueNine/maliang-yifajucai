import type { BlackListEventDocView_TargetOf_blackUserInfo_TargetOf_disInfo } from './'

/**
 * # 自然人用户信息
 */
export interface BlackListEventDocView_TargetOf_blackUserInfo {
  /**
   * 用户手机号
   */
  phone?: string | undefined
  /**
   * 全名
   */
  name?: string | undefined
  idCard?: string | undefined
  /**
   * 残疾信息
   */
  disInfo?: BlackListEventDocView_TargetOf_blackUserInfo_TargetOf_disInfo | undefined
}
