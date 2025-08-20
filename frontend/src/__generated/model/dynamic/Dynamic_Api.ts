import type { HttpMethod } from '../enums/'
import type { Dynamic_Permissions } from './'

export interface Dynamic_Api {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  name?: string | undefined
  doc?: string | undefined
  apiPath?: string | undefined
  apiMethod?: HttpMethod | undefined
  apiProtocol?: string | undefined
  permissionsId?: number | undefined
  /**
   * 所属权限
   */
  permissions?: Dynamic_Permissions | undefined
  /**
   * 该接口是否需要登录
   */
  requireLogin?: boolean | undefined
}
