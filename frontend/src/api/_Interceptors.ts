import type { dynamic, nil } from '@compose/types'
import type { KyRequest, KyResponse, NormalizedOptions } from 'ky'
import { Headers, Headers as HeadersR } from '@compose/req'
import { useUserStore } from '@/store'

export function requestSetAuthTokenFn(request: Request) {
  const authToken = window.localStorage.getItem(HeadersR.authorization)
  if (authToken != null) {
    request.headers.set(HeadersR.authorization, authToken)
  }
}

export function resetAuthTokenFn(_a: KyRequest, _b: NormalizedOptions, response: KyResponse) {
  if (!response.headers.has(HeadersR.authorization)) {
    return
  }
  const authToken = response.headers.get(HeadersR.authorization)
  if (authToken == null) {
    return
  }
  const u = useUserStore()
  u.authToken = authToken
}

/**
 * ## 拦截服务器返回的 http 错误信息
 *
 * 将 本地网络错误包装为网络错误
 */
export async function handleErrorToMessage(ctx: { data: dynamic, response: nil<Response>, error: dynamic }) {
  const type = ctx.response?.headers.get(Headers.contentType)
  const resp = ctx.response
  if (!resp) {
    return ctx.error as Error
  }

  if (type?.startsWith('application/json')) {
    const json = await resp.json() as Record<string, unknown>
    if (Object.keys(json).length === 0) {
      return ctx.error as Error
    }
    if (typeof json.code === 'number' && (typeof json.alt === 'string' || typeof json.msg === 'string')) {
      return ctx.error as Error
    }
  }
  return ctx.error as Error
}
