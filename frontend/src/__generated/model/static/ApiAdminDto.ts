import type {HttpMethod} from '../enums/';
import type {ApiAdminDto_TargetOf_permissions} from './';

export interface ApiAdminDto {
  id?: string | undefined;
  apiPath?: string | undefined;
  apiMethod?: HttpMethod | undefined;
  /**
   * 该接口是否需要登录
   */
  requireLogin?: boolean | undefined;
  /**
   * 所属权限
   */
  permissions?: ApiAdminDto_TargetOf_permissions | undefined;
}
