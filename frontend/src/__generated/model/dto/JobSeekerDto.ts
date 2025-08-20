import type { DisTyping } from '../enums/'

export interface JobSeekerDto {
  'JobSeekerV2Api/ADMIN_JOB_SEEKER': {
    id: string
    /**
     * 需要社保
     */
    rqSocial?: boolean | undefined
    /**
     * 愿意去上班
     */
    rqGotoWork?: boolean | undefined
    /**
     * 需要到达的地方
     */
    exAddressCode?: string | undefined
    /**
     * 当前户籍所在地
     */
    regAddressCode?: string | undefined
    /**
     * 用户账号
     */
    userAccount?: {
      id: string
      /**
       * 用户账号
       */
      account: string
      nickName?: string | undefined
      createUserAccount?: {
        id: string
      } | undefined
    } | undefined
    /**
     * 该简历所属的用户信息
     */
    userInfo?: {
      id: string
      /**
       * 用户手机号
       */
      phone: string
      /**
       * 备用手机号码（非紧急联系人）
       */
      sparePhone: string
      email?: string | undefined
      /**
       * 残疾信息
       */
      disInfo?: {
        id: string
        /**
         * 残疾类别
         */
        dsType?: DisTyping | undefined
        /**
         * 等级
         */
        level?: number | undefined
        /**
         * 残疾证号
         */
        certCode: string
      } | undefined
    } | undefined
  }
}
