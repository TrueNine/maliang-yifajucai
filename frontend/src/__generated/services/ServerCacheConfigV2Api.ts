import type { Executor } from '../'
import type { Dynamic_CommonKvConfigDbCache } from '../model/dynamic/'
import type { IPage, IPageParam } from '../model/static/'

export class ServerCacheConfigV2Api {
  constructor(private executor: Executor) {}

  /**
   * ## 删除百家姓
   */
  readonly deleteChinaFirstName: (options: ServerCacheConfigV2ApiOptions['deleteChinaFirstName']) => Promise<
    void
  > = async (options) => {
    let _uri = '/v2/server_cache_config/china_first_name'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.name
    _uri += _separator
    _uri += 'name='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 获取所有缓存配置数据
   *
   * @parameter {ServerCacheConfigV2ApiOptions['getAllCacheConfigData']} options
   * - pq 分页参数
   */
  readonly getAllCacheConfigData: (options: ServerCacheConfigV2ApiOptions['getAllCacheConfigData']) => Promise<
    IPage<Dynamic_CommonKvConfigDbCache>
  > = async (options) => {
    let _uri = '/v2/server_cache_config/'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.pq.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.pq.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.pq.u
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'u='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_CommonKvConfigDbCache>>
  }

  /**
   * ## 获取所有中国百家姓
   */
  readonly getChinaFirstNames: () => Promise<
    Array<string>
  > = async () => {
    const _uri = '/v2/server_cache_config/china_first_names'
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<string>>
  }

  /**
   * ## 添加百家姓
   */
  readonly patchChinaFirstName: (options: ServerCacheConfigV2ApiOptions['patchChinaFirstName']) => Promise<
    Array<string>
  > = async (options) => {
    let _uri = '/v2/server_cache_config/china_first_name'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.name
    _uri += _separator
    _uri += 'name='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'PATCH' })) as Promise<Array<string>>
  }
}

export interface ServerCacheConfigV2ApiOptions {
  deleteChinaFirstName: {
    name: string
  }
  patchChinaFirstName: {
    name: string
  }
  getChinaFirstNames: {}
  getAllCacheConfigData: {
    /**
     * 分页参数
     */
    pq: IPageParam
  }
}
