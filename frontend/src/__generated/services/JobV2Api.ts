import type { Executor } from '../'
import type { JobDto } from '../model/dto/'
import type { Dynamic_Job } from '../model/dynamic/'
import type { AuditTyping } from '../model/enums/'
import type { IPage, JobAdminSpec } from '../model/static/'

/**
 * 第二版职位 api
 */
export class JobV2Api {
  constructor(private executor: Executor) {}

  /**
   * 客户查询所有职位
   */
  readonly findAllFromCustomer: () => Promise<
    Array<Dynamic_Job>
  > = async () => {
    const _uri = '/v2/job/customer'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<Dynamic_Job>>
  }

  /**
   * ## 根据 id 查询职位（管理员接口）
   *
   * @parameter {JobV2ApiOptions['getAdminJobById']} options
   * - id jobId
   * @return 管理员可查询到的职位
   */
  readonly getAdminJobById: (options: JobV2ApiOptions['getAdminJobById']) => Promise<
    JobDto['JobV2Api/ADMIN_JOB'] | undefined
  > = async (options) => {
    let _uri = '/v2/job/admin/id/'
    _uri += encodeURIComponent(options.id)
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<JobDto['JobV2Api/ADMIN_JOB'] | undefined>
  }

  /**
   * ## 根据条件查询职位（管理员接口）
   *
   * @parameter {JobV2ApiOptions['getAdminJobBySpec']} options
   * - spec 查询条件和分页参数
   */
  readonly getAdminJobBySpec: (options: JobV2ApiOptions['getAdminJobBySpec']) => Promise<
    IPage<Dynamic_Job>
  > = async (options) => {
    const _uri = '/v2/job/admin'
    return (await this.executor({ uri: _uri, method: 'GET', body: options.body })) as Promise<IPage<Dynamic_Job>>
  }

  /**
   * ## 管理员改变职位审核状态（管理员接口）
   * TODO 管理员接口
   *
   * @parameter {JobV2ApiOptions['patchAuditStatusById']} options
   * - id 职位 id
   * - auditStatus 审核状态
   */
  readonly patchAuditStatusById: (options: JobV2ApiOptions['patchAuditStatusById']) => Promise<
    Dynamic_Job | undefined
  > = async (options) => {
    let _uri = '/v2/job/admin/auditStatus/'
    _uri += encodeURIComponent(options.id)
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.auditStatus
    _uri += _separator
    _uri += 'auditStatus='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'PATCH' })) as Promise<Dynamic_Job | undefined>
  }
}

export interface JobV2ApiOptions {
  patchAuditStatusById: {
    /**
     * 职位 id
     */
    id: number
    /**
     * 审核状态
     */
    auditStatus: AuditTyping
  }
  getAdminJobBySpec: {
    /**
     * 查询条件和分页参数
     */
    body: JobAdminSpec
  }
  getAdminJobById: {
    /**
     * jobId
     */
    id: number
  }
  findAllFromCustomer: {}
}
