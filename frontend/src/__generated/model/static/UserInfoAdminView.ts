import type { GenderTyping } from '../enums/'
import type {
  UserInfoAdminView_TargetOf_account,
  UserInfoAdminView_TargetOf_address,
  UserInfoAdminView_TargetOf_certs,
  UserInfoAdminView_TargetOf_disInfo,
  UserInfoAdminView_TargetOf_jobSeeker,
} from './'

/**
 * # 自然人用户信息
 */
export interface UserInfoAdminView {
  id: string
  /**
   * 全名
   */
  name?: string | undefined
  firstName?: string | undefined
  lastName?: string | undefined
  idCard?: string | undefined
  /**
   * 该用户信息的所有证件
   */
  certs: Array<UserInfoAdminView_TargetOf_certs>
  /**
   * 用户账号主体
   */
  account?: UserInfoAdminView_TargetOf_account | undefined
  /**
   * 地理位置编码
   */
  addressCode?: string | undefined
  /**
   * 所关联地址
   */
  address?: UserInfoAdminView_TargetOf_address | undefined
  /**
   * 用户手机号
   */
  phone?: string | undefined
  age?: number | undefined
  /**
   * 性别
   */
  gender?: GenderTyping | undefined
  /**
   * 出生日期
   */
  birthday?: string | undefined
  /**
   * 该用户所属的简历
   */
  jobSeeker?: UserInfoAdminView_TargetOf_jobSeeker | undefined
  /**
   * 残疾信息
   */
  disInfo?: UserInfoAdminView_TargetOf_disInfo | undefined
  /**
   * 备用手机号码（非紧急联系人）
   */
  sparePhone?: string | undefined
  /**
   * 是否为主信息
   */
  primaryUserInfo: boolean
  /**
   * 该用户是否被拉黑
   */
  isBlacked: boolean
  wechatOpenid?: string | undefined
}
