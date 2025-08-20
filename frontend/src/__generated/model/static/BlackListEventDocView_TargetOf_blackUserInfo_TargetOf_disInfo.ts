import type {DisTyping} from '../enums/';

/**
 * 残疾证信息
 */
export interface BlackListEventDocView_TargetOf_blackUserInfo_TargetOf_disInfo {
  /**
   * 残疾证号
   */
  certCode?: string | undefined;
  /**
   * 残疾类别
   */
  dsType?: DisTyping | undefined;
  /**
   * 等级
   */
  level?: number | undefined;
}
