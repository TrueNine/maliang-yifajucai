import type {AuditTyping, RelationItemTyping} from '../enums/';
import type {BlackListAdminView_TargetOf_blackUserInfo} from './';

export interface BlackListAdminView {
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
  /**
   * 事件描述
   */
  eventDoc: string;
  reItemType?: RelationItemTyping | undefined;
  /**
   * 被拉黑的用户信息
   */
  blackUserInfo?: BlackListAdminView_TargetOf_blackUserInfo | undefined;
}
