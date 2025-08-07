import type { latenil, task } from '@compose/types'
import type { BeforeErrorHook, Input, Options, ResponsePromise } from 'ky'
import ky from 'ky'

import { BuildConfig } from '@/config'
import { requestSetAuthTokenFn, resetAuthTokenFn } from './_Interceptors'

type ExtendOptions = Parameters<typeof ky.extend>[0]

const config: ExtendOptions = {
  throwHttpErrors: true,
  timeout: false,
  retry: 0,
  hooks: {
    beforeRequest: [requestSetAuthTokenFn],
    afterResponse: [
      resetAuthTokenFn,
      () => {
        return 123
      },
    ],
    beforeError: [] as BeforeErrorHook[],
  },
}

/**
 * http head header map
 */
export interface HeaderMap extends Record<string, latenil<string>> {
  'Content-Type'?: string
  'Access-Control-Allow-Origin'?: string
  'Access-Control-Allow-Credentials'?: string
  'Server'?: string
}

export type ProxyResponsePromise<T> = ResponsePromise<T> & {
  /**
   * ## 忽略请求体，只等到 ok 即可
   */
  abort: () => task<void>
  /**
   * ## 获取其请求头
   */
  headerMap: () => task<HeaderMap>
}

export interface OverrideKyInstance {
  <T>(url?: Input, options?: Options): ProxyResponsePromise<T>

  get: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
  post: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
  put: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
  delete: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
  patch: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
  head: <T>(url?: Input, options?: Options) => ProxyResponsePromise<T>
}

export function useApi(baseApi: string = '', otherOptions: ExtendOptions = {}): OverrideKyInstance {
  const prefix = (BuildConfig.baseUrl ?? '') + baseApi
  const __ky = ky.extend(config).extend({ prefixUrl: prefix }).extend(otherOptions)

  async function _ky<T>(url?: Input, options: Options = {}) {
    if (options.method === void 0) {
      options.method = 'get'
    }
    if (options.method.toLowerCase() === 'head') {
      options.throwHttpErrors = false
    }
    const inst = __ky(url ?? '', options) as unknown as ProxyResponsePromise<T>
    inst.abort = async () => {
      await inst.then((e) => e.ok)
    }
    inst.headerMap = async (): Promise<HeaderMap> => {
      const httpHeaders = await inst.then((e) => e.headers)
      const headerMap: HeaderMap = {}
      httpHeaders.forEach((v, key) => {
        headerMap[key] = v
      })
      return headerMap
    }
    return inst
  }

  _ky.get = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'get', ...options })
  _ky.post = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'post', ...options })
  _ky.put = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'put', ...options })
  _ky.delete = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'delete', ...options })
  _ky.patch = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'patch', ...options })
  _ky.head = async <T>(url?: Input, options: Options = {}) => _ky<T>(url, { method: 'head', ...options })
  _ky.create = __ky.create
  _ky.extend = __ky.extend

  return _ky as unknown as OverrideKyInstance
}
