import type { Executor } from '../'
import type { Dynamic_Address } from '../model/dynamic/'
import type { AddressFullPathView } from '../model/static/'

/**
 * ## 第2版地址 API
 */
export class AddressV2Api {
  constructor(private executor: Executor) {}

  /**
   * ## 查询所有省份
   */
  readonly getAllProvinces: () => Promise<
    Array<Dynamic_Address>
  > = async () => {
    const _uri = '/v2/address/provinces'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<Dynamic_Address>>
  }

  /**
   * ## 获取直接的地址直接子集
   */
  readonly getDirectChildrenByCode: (options: AddressV2ApiOptions['getDirectChildrenByCode']) => Promise<
    Array<Dynamic_Address>
  > = async (options) => {
    let _uri = '/v2/address/direct_children'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.code
    _uri += _separator
    _uri += 'code='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<Dynamic_Address>>
  }

  /**
   * ## 批量获取地址全路径
   *
   * @parameter {AddressV2ApiOptions['getFullPathsByCodes']} options
   * - codes 批量地址代码
   */
  readonly getFullPathsByCodes: (options: AddressV2ApiOptions['getFullPathsByCodes']) => Promise<
    Array<AddressFullPathView>
  > = async (options) => {
    let _uri = '/v2/address/full_paths/codes'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.codes
    for (const _item of _value) {
      _uri += _separator
      _uri += 'codes='
      _uri += encodeURIComponent(_item)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<AddressFullPathView>>
  }

  /**
   * ## 初始化省份
   * TODO ROOT 权限
   */
  readonly postInitProvincesAsAdmin: () => Promise<
    void
  > = async () => {
    const _uri = '/v2/address/init_provinces'
    return (await this.executor({ uri: _uri, method: 'POST' })) as Promise<void>
  }
}

export interface AddressV2ApiOptions {
  getDirectChildrenByCode: {
    code: string
  }
  getFullPathsByCodes: {
    /**
     * 批量地址代码
     */
    codes: Array<string>
  }
  getAllProvinces: {}
  postInitProvincesAsAdmin: {}
}
