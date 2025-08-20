import type { PlatformType } from '../enums/'

/**
 * # 菜单
 */
export interface MenuAdminSpec {
  /**
   * 匹配路径
   */
  pattern?: string | undefined
  /**
   * 平台类型
   */
  platformType?: PlatformType | undefined
  /**
   * 菜单
   */
  title?: string | undefined
  /**
   * 描述
   */
  doc?: string | undefined
  o?: number | undefined
  s?: number | undefined
}
