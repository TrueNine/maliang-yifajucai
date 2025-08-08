import type {ISO4217} from '../enums/';

/**
 * ## 银行类型
 */
export interface Dynamic_Bank {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  groupType?: string;
  bankName?: string;
  region?: ISO4217;
}
