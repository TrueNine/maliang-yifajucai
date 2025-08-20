import type { UserInfoMemberView_TargetOf_account, UserInfoMemberView_TargetOf_disInfo } from './'

/**
 * # 自然人用户信息
 */
export interface UserInfoMemberView {
  id: string
  idCard?: string | undefined
  /**
   * 主信息数据库字段
   */
  pri?: boolean | undefined
  /**
   * 此用户备注
   */
  remark?: string | undefined
  /**
   * 企业内备注名称
   */
  remarkName?: string | undefined
  /**
   * 用户账号主体
   */
  account?: UserInfoMemberView_TargetOf_account | undefined
  /**
   * 残疾信息
   */
  disInfo?: UserInfoMemberView_TargetOf_disInfo | undefined
  age?: number | undefined
  /**
   * 出生日期
   */
  birthday?: string | undefined
  /**
   * 全名
   */
  name?: string | undefined
  /**
   * 用户手机号
   */
  phone?: string | undefined
}
