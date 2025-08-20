export interface Dynamic_Address {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  code?: string;
  name?: string;
  yearVersion?: number | undefined;
  level?: number;
  center?: string | undefined;
  leaf?: boolean | undefined;
  /**
   * 当前地址的全路径
   */
  fullPath?: string | undefined;
  /**
   * 直接的父 path
   */
  parentAddresses?: Array<Dynamic_Address>;
  /**
   * 上一级地址
   */
  parentAddress?: Dynamic_Address | undefined;
  rpi?: number | undefined;
}
