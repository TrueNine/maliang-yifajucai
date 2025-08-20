import type { GenderTyping } from '../enums/'
import type {
  Dynamic_Address,
  Dynamic_BlackList,
  Dynamic_BlackListRelation,
  Dynamic_Cert,
  Dynamic_DisInfo,
  Dynamic_Enterprise,
  Dynamic_JobSeeker,
  Dynamic_UserAccount,
} from './'

/**
 * # 自然人用户信息
 */
export interface Dynamic_UserInfo {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  firstName?: string
  metadataFirstName?: string | undefined
  lastName?: string
  metadataLastName?: string | undefined
  /**
   * 全名
   */
  name?: string
  metadataName?: string | undefined
  /**
   * 出生日期
   */
  birthday?: string | undefined
  age?: number | undefined
  /**
   * 用户所关联的账号
   */
  userAccountId?: number | undefined
  /**
   * 用户账号主体
   */
  account?: Dynamic_UserAccount | undefined
  /**
   * 主信息数据库字段
   */
  pri?: boolean | undefined
  /**
   * 是否为主信息
   */
  primaryUserInfo?: boolean
  /**
   * 创建此信息的账号 id
   */
  createUserId?: number | undefined
  /**
   * 创建此信息的账号
   */
  createUserAccount?: Dynamic_UserAccount | undefined
  email?: string | undefined
  /**
   * 用户手机号
   */
  phone?: string
  metadataPhone?: string | undefined
  /**
   * 备用手机号码（非紧急联系人）
   */
  sparePhone?: string
  metadataSparePhone?: string | undefined
  idCard?: string
  metadataIdCard?: string | undefined
  /**
   * 性别
   */
  gender?: GenderTyping | undefined
  wechatOpenid?: string | undefined
  wechatAccount?: string | undefined
  wechatAuthid?: string | undefined
  qqOpenid?: string | undefined
  qqAccount?: string | undefined
  /**
   * 地理位置编码
   */
  addressCode?: string | undefined
  /**
   * 所关联地址
   */
  address?: Dynamic_Address | undefined
  /**
   * 此用户备注
   */
  remark?: string | undefined
  /**
   * 企业内备注名称
   */
  remarkName?: string
  metadataRemarkName?: string | undefined
  /**
   * 残疾信息
   */
  disInfo?: Dynamic_DisInfo | undefined
  /**
   * 该用户所属的简历
   */
  jobSeeker?: Dynamic_JobSeeker | undefined
  /**
   * 黑名单记录
   */
  blackListRecords?: Array<Dynamic_BlackList>
  /**
   * 该用户是否被拉黑
   */
  isBlacked?: boolean
  /**
   * 被拉黑次数
   */
  blackedSize?: number
  /**
   * 该用户信息的所有证件
   */
  certs?: Array<Dynamic_Cert>
  /**
   * 充当的黑名单关系
   */
  blackListRelations?: Array<Dynamic_BlackListRelation>
  /**
   * 被标记的管理的企业
   */
  markManagedEnterprises?: Array<Dynamic_Enterprise>
}
