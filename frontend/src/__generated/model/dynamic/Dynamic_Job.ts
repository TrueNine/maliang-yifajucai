import type {StartEndDateRange} from '../embeddable/';
import type {
  AuditTyping, 
  DegreeTyping, 
  EnterpriseType, 
  GenderTyping
} from '../enums/';
import type {
  Dynamic_AddressDetails, 
  Dynamic_Attachment, 
  Dynamic_Enterprise, 
  Dynamic_Industry, 
  Dynamic_JobDisNominal, 
  Dynamic_JobQueue, 
  Dynamic_JobTag, 
  Dynamic_UserAccount, 
  Dynamic_UserInfo
} from './';

/**
 * 职位
 */
export interface Dynamic_Job {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 正在等待此职位的 等待队列
   */
  waitingQueues?: Array<Dynamic_JobQueue>;
  /**
   * 标题
   */
  title?: string;
  /**
   * 职位标签
   */
  jobTags?: Array<Dynamic_JobTag>;
  /**
   * 简短描述
   */
  doc?: string | undefined;
  /**
   * 所属特殊简历信息
   */
  jobDisNominal?: Dynamic_JobDisNominal | undefined;
  /**
   * 岗位职责
   */
  postResp?: string | undefined;
  /**
   * 任职要求
   */
  qualification?: string | undefined;
  /**
   * 生命周期，持续周期，在列表内多久下架
   */
  lifecycle?: StartEndDateRange;
  /**
   * 发布职位的企业
   */
  enterprise?: Dynamic_Enterprise | undefined;
  enterpriseId?: number | undefined;
  addressDetailsId?: number | undefined;
  /**
   * 所需填写的特殊表单
   */
  rqContractFormGroupId?: number | undefined;
  /**
   * 是否为私有职位
   */
  privated?: boolean | undefined;
  /**
   * 职位准备状态
   */
  readyStatus?: number | undefined;
  /**
   * 奖金补贴规则
   */
  subsidy?: string | undefined;
  /**
   * 薪资规则
   */
  salaryCommissionRule?: string | undefined;
  /**
   * 每月薪水发放时间
   */
  payday?: number | undefined;
  /**
   * 创建时间
   */
  createDatetime?: string;
  /**
   * 工作所在地
   */
  addressCode?: string | undefined;
  /**
   * 限制员工所在地
   */
  rqAddressCode?: string | undefined;
  /**
   * 工作地所在地址
   */
  addressDetails?: Dynamic_AddressDetails | undefined;
  /**
   * 联系座机
   */
  landline?: string | undefined;
  /**
   * 联系电话
   */
  phone?: string | undefined;
  /**
   * 联系人姓名
   */
  contactName?: string | undefined;
  /**
   * 最小工资
   */
  minSalary?: number | undefined;
  /**
   * 最大工资
   */
  maxSalary?: number | undefined;
  maxManAge?: number | undefined;
  minManAge?: number | undefined;
  maxWomanAge?: number | undefined;
  minWomanAge?: number | undefined;
  /**
   * 所需行业的经验年限
   */
  exYear?: number | undefined;
  /**
   * 所需学历
   */
  degree?: DegreeTyping | undefined;
  /**
   * 职位类型
   */
  jobType?: number | undefined;
  /**
   * 从事周期
   */
  postPeriod?: string | undefined;
  /**
   * 所需残疾类别
   */
  rqDisRule?: Array<number> | undefined;
  /**
   * 岗位类型
   */
  postType?: number | undefined;
  enterpriseUserId?: number | undefined;
  /**
   * 发布者企业账号
   */
  enterpriseUserAccount?: Dynamic_UserAccount | undefined;
  /**
   * 企业类型
   */
  enterpriseType?: EnterpriseType | undefined;
  /**
   * 派遣类型
   */
  dispatchType?: number | undefined;
  industryId?: number | undefined;
  /**
   * 所属行业
   */
  industry?: Dynamic_Industry | undefined;
  /**
   * 排序权重
   */
  orderedWeight?: number;
  userInfoId?: number | undefined;
  /**
   * 发布者用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  /**
   * 性别限制
   */
  rqGender?: GenderTyping | undefined;
  /**
   * 所需人数
   */
  rqCount?: number;
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
  /**
   * 职位详情图片
   */
  detailsImages?: Array<Dynamic_Attachment>;
}
