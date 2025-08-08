import type {
  Dynamic_BlackListRelation, 
  Dynamic_Cert, 
  Dynamic_Enterprise, 
  Dynamic_JobSeekerStatus, 
  Dynamic_RoleGroup, 
  Dynamic_UserInfo
} from './';

/**
 * # 用户账号
 * 
 */
export interface Dynamic_UserAccount {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 用户账号
   */
  account?: string;
  metadataAccount?: string;
  /**
   * 加密后的密码
   */
  pwdEnc?: string;
  metadataPwdEnc?: string;
  /**
   * 该用户所属的用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  createUserAccount?: Dynamic_UserAccount | undefined;
  nickName?: string | undefined;
  doc?: string | undefined;
  banTime?: string | undefined;
  /**
   * 是否已被禁用
   */
  disabled?: boolean;
  lastLoginTime?: string | undefined;
  createUserId?: number | undefined;
  /**
   * 用户拥有的角色组
   */
  roleGroups?: Array<Dynamic_RoleGroup>;
  /**
   * 该账号的入职状态
   */
  jobSeekerStatus?: Dynamic_JobSeekerStatus | undefined;
  /**
   * 充当的黑名单关系人
   */
  blackListRelations?: Array<Dynamic_BlackListRelation>;
  /**
   * 账号对应的证件
   */
  certs?: Array<Dynamic_Cert>;
  /**
   * 该账号所管理的企业
   */
  managedEnterprises?: Array<Dynamic_Enterprise>;
}
