import type {Executor} from '../';
import type {Dynamic_JobSeekerDisNominalTaxVideo} from '../model/dynamic/';
import type {AuditTyping} from '../model/enums/';

/**
 * # 第2版个税视频接口
 */
export class TaxVideoV2Api {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 根据 id 改变 审核状态
   * TODO 管理员、审核员接口
   * 
   * @parameter {TaxVideoV2ApiOptions['patchAuditStatusById']} options
   * - id 个税视频 id
   * - auditStatus 审核状态
   */
  readonly patchAuditStatusById: (options: TaxVideoV2ApiOptions['patchAuditStatusById']) => Promise<
    Dynamic_JobSeekerDisNominalTaxVideo
  > = async(options) => {
    let _uri = '/v2/taxVideo/admin/auditStatus/';
    _uri += encodeURIComponent(options.id);
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.auditStatus;
    _uri += _separator
    _uri += 'auditStatus='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'PATCH'})) as Promise<Dynamic_JobSeekerDisNominalTaxVideo>;
  }
}

export type TaxVideoV2ApiOptions = {
  'patchAuditStatusById': {
    /**
     * 个税视频 id
     */
    id: number, 
    /**
     * 审核状态
     */
    auditStatus: AuditTyping
  }
}
