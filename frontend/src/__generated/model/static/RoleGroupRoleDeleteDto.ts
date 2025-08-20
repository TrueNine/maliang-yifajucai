import type { RoleGroupRoleDeleteDto_TargetOf_roles } from './'

export interface RoleGroupRoleDeleteDto {
  id?: string | undefined
  /**
   * 该角色组其下的角色
   */
  roles: Array<RoleGroupRoleDeleteDto_TargetOf_roles>
}
