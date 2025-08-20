import type {Dynamic_JobSeeker} from './';

/**
 * 残疾就职特殊简历
 */
export interface Dynamic_JobSeekerDisNominal {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 所属简历
   */
  jobSeeker?: Dynamic_JobSeeker;
  jobSeekerId?: number;
  /**
   * 接受证件抵押
   */
  accCollateralCert?: boolean | undefined;
  /**
   * 接受押金
   */
  accCollateralMoney?: boolean | undefined;
  /**
   * 可以去线下面签
   */
  accOfflineInterview?: boolean | undefined;
  /**
   * 现有医保
   */
  hasMedicalSupport?: boolean | undefined;
  /**
   * 拥有社保
   */
  hasSocialSupport?: boolean | undefined;
  /**
   * 拥有低保
   */
  hasLowIncomeSupport?: boolean | undefined;
}
