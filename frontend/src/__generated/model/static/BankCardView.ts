import type {BankCardView_TargetOf_certs} from './';

/**
 * 个人银行卡
 */
export interface BankCardView {
  id: string;
  code?: string | undefined;
  /**
   * 银行全名，禁止自身创建
   */
  bankName?: string | undefined;
  /**
   * 银行组织
   */
  bankGroupName?: string | undefined;
  /**
   * 结算区域，以表示国家
   */
  region?: string | undefined;
  /**
   * 于个人的使用强度排序权重
   */
  orderWeight?: number | undefined;
  available: boolean;
  /**
   * 所属证件
   */
  certs: Array<BankCardView_TargetOf_certs>;
}
