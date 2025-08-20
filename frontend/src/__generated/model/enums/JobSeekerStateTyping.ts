export const JobSeekerStateTyping_CONSTANTS = [
  /**
   * 无状态
   * 
   * 等同于空
   */
  'NONE', 
  /**
   * 等待入职
   */
  'WAITING', 
  /**
   * 已入职
   */
  'SUCCESS', 
  /**
   * 已撤离
   * 
   * 例如：去了别的平台等
   */
  'LEAVE', 
  /**
   * 不可用
   * 
   * 例如：被拉黑，被劝退
   */
  'UNAVAILABLE', 
  /**
   * 其他状态
   * 
   * 其他未来扩展状态
   */
  'OTHER'
] as const;
/**
 * 求职者状态
 */
export type JobSeekerStateTyping = typeof JobSeekerStateTyping_CONSTANTS[number];
