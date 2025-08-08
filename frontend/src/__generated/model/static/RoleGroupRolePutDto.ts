import type {RoleGroupRolePutDto_TargetOf_roles} from './';

export interface RoleGroupRolePutDto {
  id?: string | undefined;
  /**
   * 该角色组其下的角色
   */
  roles: Array<RoleGroupRolePutDto_TargetOf_roles>;
}
