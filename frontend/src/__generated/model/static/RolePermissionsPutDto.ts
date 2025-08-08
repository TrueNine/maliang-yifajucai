import type {RolePermissionsPutDto_TargetOf_permissions} from './';

/**
 * 角色 -> 权限
 */
export interface RolePermissionsPutDto {
  id?: string | undefined;
  permissions: Array<RolePermissionsPutDto_TargetOf_permissions>;
}
