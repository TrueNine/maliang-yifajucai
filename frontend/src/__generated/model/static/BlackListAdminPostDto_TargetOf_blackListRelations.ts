import type { RelationItemTyping, RelationTyping } from '../enums/'

export interface BlackListAdminPostDto_TargetOf_blackListRelations {
  /**
   * 关联账号 id
   */
  refId?: string | undefined
  userInfoId?: string | undefined
  /**
   * 此关系人的类型
   * - 用户
   * - 客户
   * - 企业
   * - ...
   */
  reItemType?: RelationItemTyping | undefined
  /**
   * 与此次事件的类型
   * - 帮凶
   * - 受害者
   * - ...
   */
  reType?: RelationTyping | undefined
}
