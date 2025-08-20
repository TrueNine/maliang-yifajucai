import type {DisTyping} from '../enums/';
import type {Dynamic_UserInfo} from './';

/**
 * 残疾证信息
 */
export interface Dynamic_DisInfo {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 残疾的具体部位
   */
  place?: Array<number> | undefined;
  /**
   * 造成残疾的原因
   */
  cause?: string | undefined;
  /**
   * 残疾证号
   */
  certCode?: string;
  metadataCertCode?: string | undefined;
  /**
   * 等级
   */
  level?: number | undefined;
  /**
   * 残疾类别
   */
  dsType?: DisTyping | undefined;
  /**
   * 所属用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  userInfoId?: number | undefined;
}
