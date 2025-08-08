import type {DisTyping} from '../enums/';

/**
 * # 自然人用户信息
 */
export interface UserInfoAdminSpec {
  /**
   * 该用户是否被拉黑
   */
  isBlacked?: boolean | undefined;
  id?: string | undefined;
  minAge?: number | undefined;
  maxAge?: number | undefined;
  /**
   * 地理位置编码
   */
  addressCode?: string | undefined;
  /**
   * 全名
   */
  name?: string | undefined;
  firstName?: string | undefined;
  lastName?: string | undefined;
  /**
   * 用户手机号
   */
  phone?: string | undefined;
  /**
   * 备用手机号码（非紧急联系人）
   */
  sparePhone?: string | undefined;
  idCard?: string | undefined;
  /**
   * 残疾类别
   */
  disInfoTypeIn?: Array<DisTyping> | undefined;
  /**
   * 等级
   */
  disInfoLevelIn?: Array<number> | undefined;
  /**
   * 残疾证号
   */
  disInfoCertCode?: string | undefined;
  /**
   * 所持有的银行卡
   */
  certsBankGroupValueIn?: Array<string> | undefined;
  o?: number | undefined;
  s?: number | undefined;
}
