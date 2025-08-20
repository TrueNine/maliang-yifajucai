import type {AuditTyping} from '../enums/';
import type {Dynamic_Attachment, Dynamic_BlackList} from './';

/**
 * # 黑名单证据
 */
export interface Dynamic_BlackListEvidence {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  attId?: number;
  /**
   * 黑名单附件
   */
  attachment?: Dynamic_Attachment;
  blackListId?: number;
  /**
   * 所属黑名单
   */
  blackList?: Dynamic_BlackList;
  /**
   * 证据描述
   */
  doc?: string | undefined;
  /**
   * 证据标题
   */
  title?: string | undefined;
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
}
