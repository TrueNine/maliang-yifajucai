import type {AuditTyping} from '../enums/';
import type {Dynamic_Attachment, Dynamic_JobSeeker, Dynamic_UserAccount} from './';

/**
 * 简历附带的个税视频？
 */
export interface Dynamic_JobSeekerDisNominalTaxVideo {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
  jobSeeker?: Dynamic_JobSeeker;
  jobSeekerId?: number;
  attId?: number;
  auditUserId?: number | undefined;
  /**
   * 审核账号
   */
  auditAccount?: Dynamic_UserAccount | undefined;
  complaintAuditUserId?: number | undefined;
  /**
   * 申诉审核账号
   */
  complaintAuditAccount?: Dynamic_UserAccount | undefined;
  /**
   * 审核时间
   */
  auditDatetime?: string;
  /**
   * 申诉时间
   */
  complaintAuditDatetime?: string;
  /**
   * 申诉审核状态
   */
  complaintAuditStatus?: AuditTyping | undefined;
  /**
   * 申诉描述
   */
  complaint?: string | undefined;
  /**
   * 出现问题的描述
   */
  problemDesc?: string | undefined;
  /**
   * 是否存在问题
   */
  problem?: boolean | undefined;
  /**
   * 个税视频查询时间
   */
  queryDatetime?: string;
  /**
   * 附带的个税视频
   */
  attachment?: Dynamic_Attachment;
}
