import type { Period } from '../static/'
import type { Dynamic_Job } from './'

/**
 * 残疾特殊职位
 */
export interface Dynamic_JobDisNominal {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  /**
   * 每月任务次数
   */
  taskOnMonthCount?: number | undefined
  /**
   * 不接受拥有医保，包括新农合
   */
  notMedicalSupport?: boolean | undefined
  /**
   * 不接受拥有社保
   */
  notSocialSupport?: boolean | undefined
  /**
   * 不接受拥有低保
   */
  notLowIncomeSupport?: boolean | undefined
  /**
   * 允许紧急情况撤出合同
   */
  exceptionOut?: boolean | undefined
  /**
   * 后续是否可续签合同
   */
  hasRenewContract?: boolean | undefined
  /**
   * 面签补贴费用
   */
  offlineInterviewAmount?: number | undefined
  /**
   * 是否需要面签
   */
  hasOfflineInterview?: boolean | undefined
  /**
   * 特殊合同期限
   */
  contractPeriod?: Period | undefined
  /**
   * 每年任务次数
   */
  taskOnYearCount?: number | undefined
  jobId?: number
  /**
   * 所属职位
   */
  job?: Dynamic_Job
}
