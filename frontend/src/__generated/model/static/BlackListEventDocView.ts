import type {BlackListEventDocView_TargetOf_blackUserInfo} from './';

export interface BlackListEventDocView {
  /**
   * 事件描述
   */
  eventDoc: string;
  /**
   * 发生日期
   */
  onDate?: string | undefined;
  /**
   * 被拉黑的用户信息
   */
  blackUserInfo?: BlackListEventDocView_TargetOf_blackUserInfo | undefined;
}
