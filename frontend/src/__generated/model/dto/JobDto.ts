import type { StartEndDateRange } from '../embeddable/'
import type {
  AuditTyping,
  DegreeTyping,
  EnterpriseType,
  GenderTyping,
} from '../enums/'

export interface JobDto {
  /**
   * 职位
   */
  'JobV2Api/ADMIN_JOB': {
    id: string
    crd?: string | undefined
    mrd?: string | undefined
    rlv: number
    /**
     * 标题
     */
    title: string
    /**
     * 简短描述
     */
    doc?: string | undefined
    /**
     * 岗位职责
     */
    postResp?: string | undefined
    /**
     * 任职要求
     */
    qualification?: string | undefined
    /**
     * 生命周期，持续周期，在列表内多久下架
     */
    lifecycle: StartEndDateRange
    /**
     * 所需填写的特殊表单
     */
    rqContractFormGroupId?: number | undefined
    /**
     * 是否为私有职位
     */
    privated?: boolean | undefined
    /**
     * 职位准备状态
     */
    readyStatus?: number | undefined
    /**
     * 奖金补贴规则
     */
    subsidy?: string | undefined
    /**
     * 薪资规则
     */
    salaryCommissionRule?: string | undefined
    /**
     * 每月薪水发放时间
     */
    payday?: number | undefined
    /**
     * 创建时间
     */
    createDatetime: string
    /**
     * 工作所在地
     */
    addressCode?: string | undefined
    /**
     * 限制员工所在地
     */
    rqAddressCode?: string | undefined
    /**
     * 联系座机
     */
    landline?: string | undefined
    /**
     * 联系电话
     */
    phone?: string | undefined
    /**
     * 联系人姓名
     */
    contactName?: string | undefined
    /**
     * 最小工资
     */
    minSalary?: number | undefined
    /**
     * 最大工资
     */
    maxSalary?: number | undefined
    maxManAge?: number | undefined
    minManAge?: number | undefined
    maxWomanAge?: number | undefined
    minWomanAge?: number | undefined
    /**
     * 所需行业的经验年限
     */
    exYear?: number | undefined
    /**
     * 所需学历
     */
    degree?: DegreeTyping | undefined
    /**
     * 职位类型
     */
    jobType?: number | undefined
    /**
     * 从事周期
     */
    postPeriod?: string | undefined
    /**
     * 所需残疾类别
     */
    rqDisRule?: Array<number> | undefined
    /**
     * 岗位类型
     */
    postType?: number | undefined
    /**
     * 企业类型
     */
    enterpriseType?: EnterpriseType | undefined
    /**
     * 派遣类型
     */
    dispatchType?: number | undefined
    /**
     * 排序权重
     */
    orderedWeight: number
    /**
     * 性别限制
     */
    rqGender?: GenderTyping | undefined
    /**
     * 所需人数
     */
    rqCount: number
    /**
     * 审核状态
     */
    auditStatus?: AuditTyping | undefined
  }
}
