import type { RelationItemTyping, RelationTyping } from '../enums/'
import type { Dynamic_BlackList, Dynamic_UserAccount, Dynamic_UserInfo } from './'

export interface Dynamic_BlackListRelation {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  /**
   * 此关系人的类型
   * - 用户
   * - 客户
   * - 企业
   * - ...
   */
  reItemType?: RelationItemTyping | undefined
  /**
   * 关系于此次事件的具体描述
   */
  eventDoc?: string | undefined
  /**
   * 与此次事件的类型
   * - 帮凶
   * - 受害者
   * - ...
   */
  reType?: RelationTyping | undefined
  blackListId?: number | undefined
  blackList?: Dynamic_BlackList | undefined
  userInfoId?: number | undefined
  /**
   * 关联的用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined
  /**
   * 关联账号 id
   */
  refId?: number | undefined
  /**
   * 关联账号
   */
  refUserAccount?: Dynamic_UserAccount | undefined
}
