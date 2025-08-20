/**
 * # 数据库缓存
 * 
 */
export interface Dynamic_CommonKvConfigDbCache {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 存储 key
   */
  k?: string;
  /**
   * 存储 value
   */
  v?: string | undefined;
}
