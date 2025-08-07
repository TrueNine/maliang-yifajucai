import type { RefId } from '@compose/types'
import type { RelationItemTyping, RelationTyping } from '@/api'

export interface BlackListRelation {
  refId?: RefId
  userInfoId?: RefId
  reType?: RelationTyping
  reItemType?: RelationItemTyping
  eventDoc?: string
}
