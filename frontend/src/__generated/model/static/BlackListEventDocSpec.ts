import type {DisTyping} from '../enums/';

export interface BlackListEventDocSpec {
  /**
   * 事件描述
   */
  eventDoc?: string | undefined;
  /**
   * 发生日期
   */
  minOnDate?: string | undefined;
  /**
   * 发生日期
   */
  maxOnDate?: string | undefined;
  /**
   * 用户手机号
   */
  blackUserInfoPhone?: string | undefined;
  /**
   * 全名
   */
  blackUserInfoName?: string | undefined;
  blackUserInfoIdCard?: string | undefined;
  /**
   * 残疾证号
   */
  blackUserInfoDisInfoCertCode?: string | undefined;
  /**
   * 残疾类别
   */
  blackUserInfoDisInfoDsType?: DisTyping | undefined;
  /**
   * 等级
   */
  blackUserInfoDisInfoLevel?: number | undefined;
}
