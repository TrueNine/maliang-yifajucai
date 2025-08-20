import type { DegreeTyping, DisTyping } from '../enums/'

export interface JobSeekerAdminSpec {
  id?: string | undefined
  /**
   * 需要到达的地方
   */
  exAddressCode?: string | undefined
  /**
   * 当前户籍所在地
   */
  regAddressCode?: string | undefined
  /**
   * 简历创建时间
   */
  maxCreateDatetime?: string | undefined
  /**
   * 学历
   */
  degree?: DegreeTyping | undefined
  accountNickName?: string | undefined
  /**
   * 用户账号
   */
  accountAccount?: string | undefined
  userInfoMaxAge?: number | undefined
  /**
   * 残疾类别
   */
  userInfoDisInfoDsType?: DisTyping | undefined
  /**
   * 等级
   */
  userInfoDisInfoLevel?: number | undefined
}
