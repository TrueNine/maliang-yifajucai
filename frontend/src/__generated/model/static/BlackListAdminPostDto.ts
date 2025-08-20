import type { RelationItemTyping } from '../enums/'
import type { BlackListAdminPostDto_TargetOf_blackListRelations } from './'

export interface BlackListAdminPostDto {
  /**
   * 事件描述
   */
  eventDoc: string
  /**
   * 黑名单事件的关系
   */
  blackListRelations: Array<BlackListAdminPostDto_TargetOf_blackListRelations>
  reportUserInfoId?: string | undefined
  reportUserId?: string | undefined
  blackUserInfoId?: string | undefined
  reItemType?: RelationItemTyping | undefined
}
