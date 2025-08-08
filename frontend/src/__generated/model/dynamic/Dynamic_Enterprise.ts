import type {EnterpriseType, ISO4217} from '../enums/';
import type {
  Dynamic_Address, 
  Dynamic_AddressDetails, 
  Dynamic_Attachment, 
  Dynamic_Industry, 
  Dynamic_Job, 
  Dynamic_UserAccount, 
  Dynamic_UserInfo
} from './';

/**
 * 企业
 */
export interface Dynamic_Enterprise {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  leaderUserInfo?: Dynamic_UserInfo | undefined;
  leaderUserInfoId?: number | undefined;
  /**
   * 该企业所绑定的账号
   */
  leaderUserAccount?: Dynamic_UserAccount | undefined;
  leaderUserAccountId?: number | undefined;
  /**
   * 数据创建时间
   */
  createDatetime?: string | undefined;
  addressDetails?: Dynamic_AddressDetails | undefined;
  addressDetailsId?: number | undefined;
  addressCode?: string | undefined;
  enterpriseType?: EnterpriseType | undefined;
  industry?: Dynamic_Industry | undefined;
  industryId?: number | undefined;
  logoAttachment?: Dynamic_Attachment | undefined;
  logoAttachmentId?: number | undefined;
  parentEnterprise?: Dynamic_Enterprise | undefined;
  childrenEnterprises?: Array<Dynamic_Enterprise>;
  rpi?: number | undefined;
  title?: string | undefined;
  employeeCount?: number | undefined;
  doc?: string | undefined;
  leaderName?: string | undefined;
  regCapital?: number | undefined;
  capitalCurrency?: ISO4217 | undefined;
  regDate?: string | undefined;
  status?: number | undefined;
  address?: Dynamic_Address | undefined;
  /**
   * 统一社会信用代码
   */
  creditCodeV1?: string | undefined;
  /**
   * 数据评分
   */
  dataScore?: number | undefined;
  /**
   * 所有发布的职位
   */
  publishedJobs?: Array<Dynamic_Job>;
}
