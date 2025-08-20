import type { UserAccountRoleGroupDeleteDto_TargetOf_roleGroups } from './'

/**
 * # 用户账号
 */
export interface UserAccountRoleGroupDeleteDto {
  id?: string | undefined
  /**
   * 用户拥有的角色组
   */
  roleGroups: Array<UserAccountRoleGroupDeleteDto_TargetOf_roleGroups>
}
