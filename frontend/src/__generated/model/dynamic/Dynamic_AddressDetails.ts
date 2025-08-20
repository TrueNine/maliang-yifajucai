import type {Dynamic_Address, Dynamic_UserAccount} from './';

/**
 * 地址详情
 */
export interface Dynamic_AddressDetails {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  userAccountId?: number;
  userAccount?: Dynamic_UserAccount;
  phone?: string;
  metadataPhone?: string | undefined;
  name?: string;
  metadataName?: string | undefined;
  addressCode?: string;
  address?: Dynamic_Address;
  addressDetails?: string;
}
