import type { GenderTyping } from '../enums/'

/**
 * # 自然人用户信息
 */
export interface UserInfoPutDto {
  firstName?: string | undefined
  lastName?: string | undefined
  idCard?: string | undefined
  /**
   * 用户手机号
   */
  phone?: string | undefined
  /**
   * 备用手机号码（非紧急联系人）
   */
  sparePhone?: string | undefined
  /**
   * 性别
   */
  gender?: GenderTyping | undefined
  email?: string | undefined
  /**
   * 出生日期
   */
  birthday?: string | undefined
  /**
   * 地理位置编码
   */
  addressCode?: string | undefined
}
