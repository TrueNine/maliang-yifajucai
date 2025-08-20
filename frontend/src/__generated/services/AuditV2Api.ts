import type { Executor } from '../'
import type { Dynamic_AnonymousCertGroup } from '../model/dynamic/'
import type { IPage, IPageParam } from '../model/static/'

/**
 * # 第2版审核API
 */
export class AuditV2Api {
  constructor(private executor: Executor) {}

  /**
   * ## 获取所有匿名证件信息
   * > 管理员权限接口，用于获取系统中所有匿名证件信息
   *
   * 该接口支持分页查询，默认返回最大分页结果
   * @parameter {AuditV2ApiOptions['getAllAnonymousCerts']} options
   * - pq 分页查询参数，默认为最大分页配置
   * @return 包含匿名证件组的分页响应结果
   */
  readonly getAllAnonymousCerts: (options: AuditV2ApiOptions['getAllAnonymousCerts']) => Promise<
    IPage<Dynamic_AnonymousCertGroup>
  > = async (options) => {
    const _uri = '/v2/audit/anonymous_certs'
    return (await this.executor({ uri: _uri, method: 'GET', body: options.body })) as Promise<IPage<Dynamic_AnonymousCertGroup>>
  }
}

export interface AuditV2ApiOptions {
  getAllAnonymousCerts: {
    /**
     * 分页查询参数，默认为最大分页配置
     */
    body: IPageParam
  }
}
