import type { PlatformType } from '../enums/'
import type { MenuAdminPutDto_TargetOf_permissions, MenuAdminPutDto_TargetOf_roles } from './'

/**
 * # 菜单
 */
export interface MenuAdminPutDto {
  /**
   * 匹配路径
   */
  pattern: string
  /**
   * 菜单
   */
  title: string
  /**
   * 描述
   */
  doc?: string | undefined
  /**
   * 是否需要登录
   */
  requireLogin: boolean
  /**
   * 平台类型
   */
  platformType: PlatformType
  /**
   * 所需角色
   */
  roles: Array<MenuAdminPutDto_TargetOf_roles>
  /**
   * 所需权限
   */
  permissions: Array<MenuAdminPutDto_TargetOf_permissions>
}
