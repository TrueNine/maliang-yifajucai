import type {PlatformType} from '../enums/';
import type {Dynamic_Permissions, Dynamic_Role} from './';

/**
 * # 菜单
 * 
 */
export interface Dynamic_Menu {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  /**
   * 平台类型
   */
  platformType?: PlatformType;
  /**
   * 菜单
   */
  title?: string;
  /**
   * 匹配路径
   */
  pattern?: string;
  /**
   * 是否需要登录
   */
  requireLogin?: boolean;
  /**
   * 描述
   */
  doc?: string | undefined;
  /**
   * 所需角色
   */
  roles?: Array<Dynamic_Role>;
  /**
   * 所需权限
   */
  permissions?: Array<Dynamic_Permissions>;
}
