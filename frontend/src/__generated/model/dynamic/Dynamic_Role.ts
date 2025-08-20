import type { Dynamic_Permissions } from './'

export interface Dynamic_Role {
  id?: string
  crd?: string | undefined
  mrd?: string | undefined
  rlv?: number
  ldf?: string | undefined
  name?: string
  doc?: string | undefined
  permissions?: Array<Dynamic_Permissions>
}
