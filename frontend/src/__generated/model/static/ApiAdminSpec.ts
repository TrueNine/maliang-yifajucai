import type { HttpMethod } from '../enums/'

export interface ApiAdminSpec {
  /**
   * 该接口是否需要登录
   */
  requireLogin?: boolean | undefined
  apiMethod?: HttpMethod | undefined
  apiPath?: string | undefined
  name?: string | undefined
  doc?: string | undefined
  permissionsId?: string | undefined
  permissionsName?: string | undefined
  permissionsDoc?: string | undefined
  o?: number | undefined
  s?: number | undefined
}
