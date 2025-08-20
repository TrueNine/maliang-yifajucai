import type { Dynamic_Role, Dynamic_UserAccount } from './'

export interface Dynamic_RoleGroup {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  name?: string
  doc?: string | undefined
  /**
   * 拥有该角色组的用户账号
   */
  userAccounts?: Array<Dynamic_UserAccount>
  /**
   * 该角色组其下的角色
   */
  roles?: Array<Dynamic_Role>
}
