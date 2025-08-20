import type {Dynamic_Cert, Dynamic_UserAccount} from './';

/**
 * 个人银行卡
 */
export interface Dynamic_BankCard {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  available?: boolean;
  /**
   * 于个人的使用强度排序权重
   */
  orderWeight?: number | undefined;
  /**
   * 结算区域，以表示国家
   */
  region?: string | undefined;
  /**
   * 银行全名，禁止自身创建
   */
  bankName?: string | undefined;
  /**
   * 银行组织
   */
  bankGroupName?: string | undefined;
  userAccount?: Dynamic_UserAccount;
  userAccountId?: number;
  code?: string | undefined;
  /**
   * 所属证件
   */
  certs?: Array<Dynamic_Cert>;
  visible?: boolean | undefined;
  /**
   * 联系手机
   */
  phone?: string;
  /**
   * 开户行地址
   */
  issueLocation?: string | undefined;
}
