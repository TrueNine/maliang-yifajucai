import type { AuditTyping, RelationItemTyping } from '../enums/'
import type {
  Dynamic_BlackListEvidence,
  Dynamic_BlackListRelation,
  Dynamic_BlackListTag,
  Dynamic_UserAccount,
  Dynamic_UserInfo,
} from './'

export interface Dynamic_BlackList {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  /**
   * 黑名单标签
   */
  blackListTags?: Array<Dynamic_BlackListTag>
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined
  createUserId?: number | undefined
  /**
   * 创建此黑名单的用户
   */
  createUserAccount?: Dynamic_UserAccount | undefined
  reportUserId?: number | undefined
  /**
   * 报告者账号
   */
  reportUserAccount?: Dynamic_UserAccount | undefined
  reportUserInfoId?: number | undefined
  /**
   * 报告者的用户信息
   */
  reportUserInfo?: Dynamic_UserInfo | undefined
  /**
   * 被拉黑的账号
   */
  blackRefId?: number | undefined
  blackUserInfoId?: number | undefined
  /**
   * 被拉黑的用户信息
   */
  blackUserInfo?: Dynamic_UserInfo | undefined
  /**
   * 被拉黑的账号
   */
  blackRefUserAccount?: Dynamic_UserAccount | undefined
  reItemType?: RelationItemTyping | undefined
  createDatetime?: string | undefined
  /**
   * 事件描述
   */
  eventDoc?: string
  /**
   * 发生日期
   */
  onDate?: string | undefined
  /**
   * 黑名单所有证据
   */
  evidences?: Array<Dynamic_BlackListEvidence>
  /**
   * 黑名单事件的关系
   */
  blackListRelations?: Array<Dynamic_BlackListRelation>
}
