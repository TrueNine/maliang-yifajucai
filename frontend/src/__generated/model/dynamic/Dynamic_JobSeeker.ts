import type {ExSalaryRange} from '../embeddable/';
import type {DegreeTyping} from '../enums/';
import type {
  Dynamic_Industry, 
  Dynamic_JobSeekerDisNominal, 
  Dynamic_JobSeekerDisNominalTaxVideo, 
  Dynamic_JobSeekerStatus, 
  Dynamic_UserAccount, 
  Dynamic_UserInfo
} from './';

export interface Dynamic_JobSeeker {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  userAccountId?: number | undefined;
  /**
   * 用户账号
   */
  userAccount?: Dynamic_UserAccount | undefined;
  /**
   * 需要到达的地方
   */
  exAddressCode?: string | undefined;
  /**
   * 接受远程工作
   */
  exRemote?: boolean | undefined;
  /**
   * 需要社保
   */
  rqSocial?: boolean | undefined;
  exIndustryId?: number | undefined;
  /**
   * 期望行业
   */
  exIndustry?: Dynamic_Industry | undefined;
  /**
   * 当前户籍所在地
   */
  regAddressCode?: string | undefined;
  /**
   * 想要的工资范围
   */
  salaryRange?: ExSalaryRange | undefined;
  /**
   * 简历创建时间
   */
  createDatetime?: string;
  /**
   * 学历
   */
  degree?: DegreeTyping | undefined;
  /**
   * 愿意去上班
   */
  rqGotoWork?: boolean | undefined;
  /**
   * 残疾就职特殊简历
   */
  jobSeekerDisNominal?: Dynamic_JobSeekerDisNominal | undefined;
  /**
   * 该简历的入职状态
   */
  jobSeekerStatus?: Dynamic_JobSeekerStatus | undefined;
  userInfoId?: number | undefined;
  /**
   * 该简历所属的用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  /**
   * 所有的个税视频
   */
  taxVideos?: Array<Dynamic_JobSeekerDisNominalTaxVideo>;
}
