import type {AuditTyping, JobSeekerStateTyping} from '../enums/';
import type {Dynamic_JobSeeker, Dynamic_UserAccount} from './';

/**
 * 简历的入职状态
 */
export interface Dynamic_JobSeekerStatus {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  jobId?: number | undefined;
  userAccountId?: number;
  /**
   * 所属账号
   */
  userAccount?: Dynamic_UserAccount;
  jobSeekerId?: number;
  /**
   * 所属简历
   */
  jobSeeker?: Dynamic_JobSeeker;
  auditUserId?: number | undefined;
  /**
   * 入职状态
   */
  status?: JobSeekerStateTyping | undefined;
  /**
   * 创建时间
   */
  createdDatetime?: string;
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
  /**
   * 审核人账号
   */
  auditUserAccount?: Dynamic_UserAccount | undefined;
  /**
   * 金额
   */
  amount?: number | undefined;
  /**
   * 备注
   */
  remark?: string | undefined;
}
