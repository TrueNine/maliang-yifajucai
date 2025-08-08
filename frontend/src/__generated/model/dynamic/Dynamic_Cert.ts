import type {
  AuditTyping, 
  CertContentTyping, 
  CertPointTyping, 
  CertTyping
} from '../enums/';
import type {Dynamic_Attachment, Dynamic_UserAccount, Dynamic_UserInfo} from './';

/**
 * 用户证件
 */
export interface Dynamic_Cert {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  name?: string | undefined;
  doc?: string | undefined;
  remark?: string | undefined;
  createDatetime?: string | undefined;
  createIp?: string | undefined;
  createDeviceId?: string | undefined;
  createUserId?: number | undefined;
  /**
   * 创建此证件的账号
   */
  createUserAccount?: Dynamic_UserAccount | undefined;
  /**
   * 审核状态
   */
  auditStatus?: AuditTyping | undefined;
  attId?: number;
  /**
   * 原始证件
   */
  metaAttachment?: Dynamic_Attachment;
  wmAttId?: number | undefined;
  /**
   * 水印图片
   */
  waterMarkerAttachment?: Dynamic_Attachment | undefined;
  userInfoId?: number | undefined;
  /**
   * 所属用户信息
   */
  userInfo?: Dynamic_UserInfo | undefined;
  /**
   * 所属账号
   */
  userAccount?: Dynamic_UserAccount | undefined;
  userId?: number | undefined;
  /**
   * 证件印面类型
   */
  poType?: CertPointTyping | undefined;
  /**
   * 证件内容类型
   */
  coType?: CertContentTyping | undefined;
  /**
   * 证件类型
   */
  doType?: CertTyping | undefined;
  /**
   * 水印码
   * 
   * water marker code
   */
  wmCode?: string | undefined;
  /**
   * 证件组编号
   */
  groupCode?: string | undefined;
  /**
   * 对于用户是否可见
   */
  visible?: boolean | undefined;
}
