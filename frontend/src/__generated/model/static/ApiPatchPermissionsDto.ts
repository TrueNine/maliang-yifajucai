export interface ApiPatchPermissionsDto {
  id?: string | undefined
  /**
   * 该接口是否需要登录
   */
  requireLogin?: boolean | undefined
  permissionsId?: string | undefined
}
