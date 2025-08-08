export const EnterpriseType_CONSTANTS = [
  'NONE', 
  /**
   * 中介
   */
  'MIDDLEMAN', 
  /**
   * 企业
   */
  'ENTERPRISE', 
  /**
   * 其他
   */
  'OTHER'
] as const;
/**
 * 企业类别
 */
export type EnterpriseType = typeof EnterpriseType_CONSTANTS[number];
