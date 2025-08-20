import type {
  Dynamic_Job, 
  Dynamic_JobSeeker, 
  Dynamic_UserAccount, 
  Dynamic_UserInfo
} from './';

/**
 * 职位入职队列
 */
export interface Dynamic_JobQueue {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 排序权重
   */
  orderWeight?: number;
  /**
   * 取消，撤离时间
   */
  cancelDatetime?: string | undefined;
  /**
   * 是否已被取消
   */
  canceled?: boolean;
  /**
   * 创建时间
   */
  createDatetime?: string;
  userInfoId?: number | undefined;
  /**
   * 应聘用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  userId?: number | undefined;
  /**
   * 应聘用户
   */
  userAccount?: Dynamic_UserAccount | undefined;
  jobSeekerId?: number;
  /**
   * 所属简历
   */
  jobSeeker?: Dynamic_JobSeeker;
  jobId?: number;
  /**
   * 所属职位
   */
  job?: Dynamic_Job;
}
