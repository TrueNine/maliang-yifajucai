import type { Executor } from '@/__generated'
import { Headers } from '@compose/req'
import { HTTPError } from 'ky'
import { Api } from '@/__generated'
import { useApi } from '@/api/_Ky'
import { isMobile } from '@/common'
import { eventBus } from '@/common/event-bus'
import { Router } from '@/router'

export type { ElementOf, RequestOf, ResponseOf } from '@/__generated'
export type * as dtos from '@/__generated/model/dto'
export type * as entities from '@/__generated/model/dynamic'
export * as enums from '@/__generated/model/enums'
export * from '@/__generated/model/enums'

const kyApi = useApi()

const executor: Executor = async ({ method, uri, headers, body }) => {
  type HttpMethod = typeof method & 'HEAD'
  const httpMethod = method as unknown as HttpMethod

  const normalizedUri = uri.startsWith('/') ? uri.slice(1) : uri

  const isFormData = body instanceof FormData
  const requestOptions = {
    method: httpMethod,
    headers,
    ...(isFormData
      ? { body }
      : body !== null && body !== void 0
        ? { json: body as Record<string, unknown> }
        : {}),
  }

  const response = kyApi(normalizedUri, requestOptions)
  if (httpMethod === 'HEAD') {
    return response.headerMap()
  }

  try {
    const resp = await response
    return await handleResponse(resp)
  } catch (error) {
    await handleError(error)
    throw error
  }
}

async function handleResponse(response: Response): Promise<string | object | Blob> {
  const contentType = response.headers.get(Headers.contentType) ?? 'text/plain'

  const contentHandlers: Record<string, () => Promise<string | object | Blob>> = {
    'text/plain': async () => response.text(),
    'application/json': async () => response.json() as Promise<object>,
    'application/octet-stream': async () => response.blob(),
  } as const

  const handler = Object.entries(contentHandlers).find(([type]) =>
    contentType.startsWith(type),
  )?.[1]

  return handler?.() ?? response.text()
}

async function handleError(error: unknown): Promise<void> {
  if (error instanceof HTTPError) {
    // 尝试从错误响应中提取 JSON 数据
    interface ErrorResponse {
      msg?: string
      alt?: string
      code?: number
    }

    let errorData: ErrorResponse | null = null
    // 检查响应的 Content-Type 是否为 JSON
    const contentType = error.response.headers.get(Headers.contentType)
    const isJsonResponse = contentType !== null
      && contentType !== ''
      && contentType.includes('application/json')

    if (isJsonResponse === true) {
      try {
        errorData = await error.response.clone().json() as ErrorResponse
      } catch {
        errorData = null
      }
    }

    // 提前计算错误消息
    const getErrorMessage = (defaultMessage: string): string => {
      if (errorData === null) {
        return defaultMessage
      }

      if (typeof errorData.msg === 'string' && errorData.msg !== '') {
        return errorData.msg
      }

      if (typeof errorData.alt === 'string' && errorData.alt !== '') {
        return errorData.alt
      }

      return defaultMessage
    }

    const errorHandlers: Record<number, () => Promise<void>> = {
      401: async () => {
        const authPath = isMobile() ? '/wxpa/auth' : '/auth'
        await Router.push(authPath)
        eventBus.emitNotification({
          type: 'error',
          message: getErrorMessage('请先登录'),
        })
      },
      403: async () => {
        Router.back()
        eventBus.emitNotification({
          type: 'warning',
          message: getErrorMessage('权限不足'),
        })
      },
    }

    const handler = errorHandlers[error.response.status]
    await handler?.()

    if (handler === void 0 && errorData !== null) {
      eventBus.emitNotification({
        type: 'error',
        message: getErrorMessage(`请求失败 (${error.response.status})`),
      })
    }
  } else {
    eventBus.emitNotification({
      type: 'error',
      message: String(error),
    })
  }
}

export const api = new Api(executor)
