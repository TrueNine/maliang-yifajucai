import type {UserAccountRoleGroupPutDto_TargetOf_roleGroups} from './';

/**
 * 用户账号 -> 角色组
 */
export interface UserAccountRoleGroupPutDto {
  id?: string | undefined;
  /**
   * 用户拥有的角色组
   */
  roleGroups: Array<UserAccountRoleGroupPutDto_TargetOf_roleGroups>;
}
