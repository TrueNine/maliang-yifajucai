import type { Executor } from '../'
import type {
  Dynamic_Api,
  Dynamic_Menu,
  Dynamic_Permissions,
  Dynamic_Role,
  Dynamic_RoleGroup,
  Dynamic_UserAccount,
} from '../model/dynamic/'
import type {
  ApiAdminDto,
  ApiAdminSpec,
  ApiPatchPermissionsDto,
  IPage,
  MenuAdminMenuVersionSpec,
  MenuAdminPutDto,
  MenuAdminSpec,
  PermissionsAdminPostDto,
  PermissionsAdminSpec,
  RoleAdminSpec,
  RoleGroupAdminSpec,
  RoleGroupPostDto,
  RoleGroupPutDto,
  RoleGroupRoleAdminSpec,
  RoleGroupRoleDeleteDto,
  RoleGroupRolePutDto,
  RolePermissionsAdminSpec,
  RolePermissionsPutDto,
  RolePostDto,
  RolePutDto,
  UserAccountAdminView,
  UserAccountRoleGroupAdminSpec,
  UserAccountRoleGroupDeleteDto,
  UserAccountRoleGroupPutDto,
} from '../model/static/'

/**
 * # 访问控制 API
 *
 * 该 API 注重于 直接的 RBAC 权限直接管理，并非直接控制账号
 *
 */
export class AclV2Api {
  constructor(private executor: Executor) {}

  /**
   * ## 根据 id 删除权限
   */
  readonly deletePermissionsByIdAsAdmin: (options: AclV2ApiOptions['deletePermissionsByIdAsAdmin']) => Promise<
    void
  > = async (options) => {
    let _uri = '/v2/acl/permissions'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.id
    _uri += _separator
    _uri += 'id='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 根据 id 删除角色
   */
  readonly deleteRoleByIdAsAdmin: (options: AclV2ApiOptions['deleteRoleByIdAsAdmin']) => Promise<
    void
  > = async (options) => {
    let _uri = '/v2/acl/role'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.id
    _uri += _separator
    _uri += 'id='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 根据 id 删除角色组
   */
  readonly deleteRoleGroupByIdAsAdmin: (options: AclV2ApiOptions['deleteRoleGroupByIdAsAdmin']) => Promise<
    void
  > = async (options) => {
    let _uri = '/v2/acl/role_group'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.id
    _uri += _separator
    _uri += 'id='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    return (await this.executor({ uri: _uri, method: 'DELETE' })) as Promise<void>
  }

  /**
   * ## 删除角色组关联角色 > 根据传入的 DTO 删除角色组与角色的关联关系
   *
   * 该接口用于移除角色组中指定的角色关联，操作成功后返回更新后的角色组信息
   *
   * @parameter {AclV2ApiOptions['deleteRoleGroupRoles']} options
   * - dto 角色组角色删除数据传输对象，包含要删除的角色组ID和角色ID列表
   * @return 更新后的角色组实体
   */
  readonly deleteRoleGroupRoles: (options: AclV2ApiOptions['deleteRoleGroupRoles']) => Promise<
    Dynamic_RoleGroup
  > = async (options) => {
    const _uri = '/v2/acl/role_group_roles'
    return (await this.executor({ uri: _uri, method: 'DELETE', body: options.body })) as Promise<Dynamic_RoleGroup>
  }

  /**
   * ## 删除角色下的权限 > 管理员权限操作，用于删除指定角色的权限
   *
   * 该接口需要`ADMIN`权限，通过传入的角色权限DTO对象，更新角色权限信息。
   *
   * @parameter {AclV2ApiOptions['deleteRolePermissions']} options
   * - dto 角色权限更新DTO对象，包含需要更新的角色ID和权限列表
   * @return 更新后的角色实体对象
   */
  readonly deleteRolePermissions: (options: AclV2ApiOptions['deleteRolePermissions']) => Promise<
    Dynamic_Role
  > = async (options) => {
    const _uri = '/v2/acl/role_permissions'
    return (await this.executor({ uri: _uri, method: 'DELETE', body: options.body })) as Promise<Dynamic_Role>
  }

  /**
   * ## 删除用户账户角色组 > 管理员权限操作，根据条件删除用户账户角色组
   *
   * 该接口用于管理员根据指定条件删除用户账户角色组，操作成功后返回更新后的用户账户信息。
   *
   * @parameter {AclV2ApiOptions['deleteUserAccountRoleGroupAsAdmin']} options
   * - dto 删除条件DTO，包含删除用户账户角色组所需的条件信息
   * @return 更新后的用户账户信息
   */
  readonly deleteUserAccountRoleGroupAsAdmin: (options: AclV2ApiOptions['deleteUserAccountRoleGroupAsAdmin']) => Promise<
    Dynamic_UserAccount
  > = async (options) => {
    const _uri = '/v2/acl/user_account_role_groups'
    return (await this.executor({ uri: _uri, method: 'DELETE', body: options.body })) as Promise<Dynamic_UserAccount>
  }

  /**
   * ## 查询 角色下的权限
   */
  readonly getALlRolePermissionsAsAdmin: (options: AclV2ApiOptions['getALlRolePermissionsAsAdmin']) => Promise<
    IPage<Dynamic_Permissions>
  > = async (options) => {
    let _uri = '/v2/acl/role_permissions'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsId
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsId='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsName
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsName='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsDoc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsDoc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Permissions>>
  }

  /**
   * ## 获取所有 API
   */
  readonly getAllApisAsAdmin: (options: AclV2ApiOptions['getAllApisAsAdmin']) => Promise<
    IPage<Dynamic_Api>
  > = async (options) => {
    let _uri = '/v2/acl/apis'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.requireLogin
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'requireLogin='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.apiMethod
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'apiMethod='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.apiPath
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'apiPath='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsId
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsId='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsName
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsName='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.permissionsDoc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsDoc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Api>>
  }

  /**
   * ## 获取所有权限列表
   */
  readonly getAllPermissionsAsAdmin: (options: AclV2ApiOptions['getAllPermissionsAsAdmin']) => Promise<
    IPage<Dynamic_Permissions>
  > = async (options) => {
    let _uri = '/v2/acl/permissions'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Permissions>>
  }

  /**
   * ## 查询角色组其下的角色
   */
  readonly getAllRoleGroupRolesAsAdmin: (options: AclV2ApiOptions['getAllRoleGroupRolesAsAdmin']) => Promise<
    IPage<Dynamic_Role>
  > = async (options) => {
    let _uri = '/v2/acl/role_group_roles'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.rolesId
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'rolesId='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.rolesName
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'rolesName='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.rolesDoc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'rolesDoc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Role>>
  }

  /**
   * ## 管理员获取所有角色组
   *
   * @parameter {AclV2ApiOptions['getAllRoleGroupsAsAdmin']} options
   * - spec 角色组查询条件
   */
  readonly getAllRoleGroupsAsAdmin: (options: AclV2ApiOptions['getAllRoleGroupsAsAdmin']) => Promise<
    IPage<Dynamic_RoleGroup>
  > = async (options) => {
    let _uri = '/v2/acl/role_groups'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_RoleGroup>>
  }

  /**
   * ## 客户端传入 menu 以服务器进行校验
   *
   * @parameter {AclV2ApiOptions['getClientMenuVersion']} options
   * - menus 客户端传入的菜单路径
   */
  readonly getClientMenuVersion: (options: AclV2ApiOptions['getClientMenuVersion']) => Promise<
    string
  > = async (options) => {
    let _uri = '/v2/acl/client_menu_version'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.menus
    for (const _item of _value) {
      _uri += _separator
      _uri += 'menus='
      _uri += encodeURIComponent(_item)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<string>
  }

  /**
   * ## 查询菜单
   *
   * @parameter {AclV2ApiOptions['getMenus']} options
   * - spec 查询参数
   */
  readonly getMenus: (options: AclV2ApiOptions['getMenus']) => Promise<
    IPage<Dynamic_Menu>
  > = async (options) => {
    let _uri = '/v2/acl/menus'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.pattern
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'pattern='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.platformType
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'platformType='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.title
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'title='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Menu>>
  }

  readonly getRoleGroupUserAccounts: (options: AclV2ApiOptions['getRoleGroupUserAccounts']) => Promise<
    IPage<UserAccountAdminView>
  > = async (options) => {
    let _uri = '/v2/acl/role_group_user_accounts'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.roleGroupIds
    if (_value !== undefined && _value !== null) {
      for (const _item of _value) {
        _uri += _separator
        _uri += 'roleGroupIds='
        _uri += encodeURIComponent(_item)
        _separator = '&'
      }
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<UserAccountAdminView>>
  }

  /**
   * ## 获取所有角色
   */
  readonly getRolesAsAdmin: (options: AclV2ApiOptions['getRolesAsAdmin']) => Promise<
    IPage<Dynamic_Role>
  > = async (options) => {
    let _uri = '/v2/acl/roles'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.name
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'name='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.o
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'o='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.s
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 's='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<IPage<Dynamic_Role>>
  }

  /**
   * ## 获取服务端菜单版本
   * 1. 查询所有 pattern 字符串
   * 2. 对所有 pattern 进行排序并拼接
   * 3. 进行 base64 编码
   * 4. 进行 sha256 编码
   *
   * @return 服务端菜单版本 （如果为空则返回空字符串）
   */
  readonly getServerMenuVersion: (options: AclV2ApiOptions['getServerMenuVersion']) => Promise<
    string
  > = async (options) => {
    let _uri = '/v2/acl/server_menu_version'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.platformType
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'platformType='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<string>
  }

  /**
   * ## 修改 API 的权限
   */
  readonly patchApiPermissions: (options: AclV2ApiOptions['patchApiPermissions']) => Promise<
    Dynamic_Api
  > = async (options) => {
    let _uri = '/v2/acl/api'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.dto.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.dto.requireLogin
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'requireLogin='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.dto.permissionsId
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'permissionsId='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'PATCH' })) as Promise<Dynamic_Api>
  }

  /**
   * ## 保存新的 API
   */
  readonly postApiAsAdmin: (options: AclV2ApiOptions['postApiAsAdmin']) => Promise<
    Dynamic_Api
  > = async (options) => {
    const _uri = '/v2/acl/api'
    return (await this.executor({ uri: _uri, method: 'POST', body: options.body })) as Promise<Dynamic_Api>
  }

  /**
   * ## 创建或者更新菜单
   *
   * @parameter {AclV2ApiOptions['postMenuByPatternAsAdmin']} options
   * - dto 保存的菜单信息
   */
  readonly postMenuByPatternAsAdmin: (options: AclV2ApiOptions['postMenuByPatternAsAdmin']) => Promise<
    Dynamic_Menu
  > = async (options) => {
    const _uri = '/v2/acl/menu'
    return (await this.executor({ uri: _uri, method: 'POST', body: options.body })) as Promise<Dynamic_Menu>
  }

  /**
   * ## 创建权限
   */
  readonly postPermissions: (options: AclV2ApiOptions['postPermissions']) => Promise<
    Dynamic_Permissions
  > = async (options) => {
    let _uri = '/v2/acl/permissions'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.dto.name
    _uri += _separator
    _uri += 'name='
    _uri += encodeURIComponent(_value)
    _separator = '&'
    _value = options.dto.doc
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'doc='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'POST' })) as Promise<Dynamic_Permissions>
  }

  /**
   * ## 创建角色
   *
   * @parameter {AclV2ApiOptions['postRoleAsAdmin']} options
   * - dto 角色信息
   */
  readonly postRoleAsAdmin: (options: AclV2ApiOptions['postRoleAsAdmin']) => Promise<
    Dynamic_Role
  > = async (options) => {
    const _uri = '/v2/acl/role'
    return (await this.executor({ uri: _uri, method: 'POST', body: options.body })) as Promise<Dynamic_Role>
  }

  /**
   * ## 创建角色组
   */
  readonly postRoleGroupAsAdmin: (options: AclV2ApiOptions['postRoleGroupAsAdmin']) => Promise<
    Dynamic_RoleGroup
  > = async (options) => {
    const _uri = '/v2/acl/role_group'
    return (await this.executor({ uri: _uri, method: 'POST', body: options.body })) as Promise<Dynamic_RoleGroup>
  }

  /**
   * ## 更新角色权限 > 根据传入的角色权限信息，更新系统中的角色权限配置
   *
   * 该接口用于更新指定角色的权限信息，系统会根据传入的权限列表重新配置角色的权限。 更新操作会覆盖原有的权限配置，请确保传入的权限列表是完整的。
   *
   * @parameter {AclV2ApiOptions['postRolePermissions']} options
   * - dto 角色权限更新数据传输对象，包含角色ID和新的权限列表
   * @return 更新后的角色实体对象，包含最新的权限配置信息
   */
  readonly postRolePermissions: (options: AclV2ApiOptions['postRolePermissions']) => Promise<
    Dynamic_Role
  > = async (options) => {
    const _uri = '/v2/acl/role_permissions'
    return (await this.executor({ uri: _uri, method: 'POST', body: options.body })) as Promise<Dynamic_Role>
  }

  /**
   * ## 修改 API
   */
  readonly putApiAsAdmin: (options: AclV2ApiOptions['putApiAsAdmin']) => Promise<
    Dynamic_Api
  > = async (options) => {
    const _uri = '/v2/acl/api'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Dynamic_Api>
  }

  /**
   * ## 批量更新菜单信息（管理员权限） > 该接口用于管理员批量更新系统菜单信息，会先清空现有菜单数据，然后插入新的菜单数据
   *
   * 该接口执行以下操作：
   * 1. 校验传入的菜单数据是否合法
   * 2. 清空现有菜单数据
   * 3. 批量插入新的菜单数据
   * 4. 记录操作日志
   *
   * @parameter {AclV2ApiOptions['putMenusAsAdmin']} options
   * - dtos 菜单更新DTO列表，包含菜单的详细信息
   * @return 更新后的菜单实体列表
   */
  readonly putMenusAsAdmin: (options: AclV2ApiOptions['putMenusAsAdmin']) => Promise<
    Array<Dynamic_Menu>
  > = async (options) => {
    const _uri = '/v2/acl/menus'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Array<Dynamic_Menu>>
  }

  /**
   * ## 更新角色信息
   *
   * @parameter {AclV2ApiOptions['putRoleAsAdmin']} options
   * - dto 更新的角色信息
   */
  readonly putRoleAsAdmin: (options: AclV2ApiOptions['putRoleAsAdmin']) => Promise<
    Dynamic_Role
  > = async (options) => {
    const _uri = '/v2/acl/role'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Dynamic_Role>
  }

  /**
   * ## 更新角色组信息
   */
  readonly putRoleGroupAsAdmin: (options: AclV2ApiOptions['putRoleGroupAsAdmin']) => Promise<
    Dynamic_RoleGroup
  > = async (options) => {
    const _uri = '/v2/acl/role_group'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Dynamic_RoleGroup>
  }

  /**
   * ## 更新角色组与角色的关联关系 > 根据传入的DTO对象，更新角色组与角色的关联关系
   *
   * 该接口用于批量更新角色组与角色的关联关系，支持添加或移除角色组中的角色。 接口采用幂等设计，多次调用结果一致。
   *
   * @parameter {AclV2ApiOptions['putRoleGroupRoles']} options
   * - dto 角色组角色更新DTO对象，包含角色组ID和待更新的角色ID列表
   * @return 更新后的角色组实体对象
   */
  readonly putRoleGroupRoles: (options: AclV2ApiOptions['putRoleGroupRoles']) => Promise<
    Dynamic_RoleGroup
  > = async (options) => {
    const _uri = '/v2/acl/role_group_roles'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Dynamic_RoleGroup>
  }

  /**
   * ## 更新用户账户角色组（管理员权限） > 该接口仅限管理员权限调用，用于更新指定用户账户的角色组信息
   *
   * 接口路径：`PUT /user_account_role_groups` 权限要求：管理员权限
   *
   * @parameter {AclV2ApiOptions['putUserAccountRoleGroupsAsAdmin']} options
   * - dto 用户账户角色组更新DTO，包含以下字段：
   * @return 更新后的用户账户实体
   */
  readonly putUserAccountRoleGroupsAsAdmin: (options: AclV2ApiOptions['putUserAccountRoleGroupsAsAdmin']) => Promise<
    Dynamic_UserAccount
  > = async (options) => {
    const _uri = '/v2/acl/user_account_role_groups'
    return (await this.executor({ uri: _uri, method: 'PUT', body: options.body })) as Promise<Dynamic_UserAccount>
  }
}

export interface AclV2ApiOptions {
  putUserAccountRoleGroupsAsAdmin: {
    /**
     * 用户账户角色组更新DTO，包含以下字段：
     */
    body: UserAccountRoleGroupPutDto
  }
  deleteUserAccountRoleGroupAsAdmin: {
    /**
     * 删除条件DTO，包含删除用户账户角色组所需的条件信息
     */
    body: UserAccountRoleGroupDeleteDto
  }
  postRolePermissions: {
    /**
     * 角色权限更新数据传输对象，包含角色ID和新的权限列表
     */
    body: RolePermissionsPutDto
  }
  deleteRolePermissions: {
    /**
     * 角色权限更新DTO对象，包含需要更新的角色ID和权限列表
     */
    body: RolePermissionsPutDto
  }
  getAllPermissionsAsAdmin: {
    spec: PermissionsAdminSpec
  }
  postApiAsAdmin: {
    body: ApiAdminDto
  }
  putApiAsAdmin: {
    body: ApiAdminDto
  }
  postRoleGroupAsAdmin: {
    body: RoleGroupPostDto
  }
  getAllApisAsAdmin: {
    spec: ApiAdminSpec
  }
  patchApiPermissions: {
    dto: ApiPatchPermissionsDto
  }
  deleteRoleByIdAsAdmin: {
    id: number
  }
  deleteRoleGroupByIdAsAdmin: {
    id: number
  }
  deletePermissionsByIdAsAdmin: {
    id: number
  }
  getMenus: {
    /**
     * 查询参数
     */
    spec: MenuAdminSpec
  }
  getRolesAsAdmin: {
    spec: RoleAdminSpec
  }
  putMenusAsAdmin: {
    /**
     * 菜单更新DTO列表，包含菜单的详细信息
     */
    body: Array<MenuAdminPutDto>
  }
  postMenuByPatternAsAdmin: {
    /**
     * 保存的菜单信息
     */
    body: MenuAdminPutDto
  }
  getClientMenuVersion: {
    /**
     * 客户端传入的菜单路径
     */
    menus: Array<string>
  }
  postRoleAsAdmin: {
    /**
     * 角色信息
     */
    body: RolePostDto
  }
  putRoleAsAdmin: {
    /**
     * 更新的角色信息
     */
    body: RolePutDto
  }
  putRoleGroupAsAdmin: {
    body: RoleGroupPutDto
  }
  postPermissions: {
    dto: PermissionsAdminPostDto
  }
  getServerMenuVersion: {
    spec: MenuAdminMenuVersionSpec
  }
  getALlRolePermissionsAsAdmin: {
    spec: RolePermissionsAdminSpec
  }
  getAllRoleGroupRolesAsAdmin: {
    spec: RoleGroupRoleAdminSpec
  }
  putRoleGroupRoles: {
    /**
     * 角色组角色更新DTO对象，包含角色组ID和待更新的角色ID列表
     */
    body: RoleGroupRolePutDto
  }
  deleteRoleGroupRoles: {
    /**
     * 角色组角色删除数据传输对象，包含要删除的角色组ID和角色ID列表
     */
    body: RoleGroupRoleDeleteDto
  }
  getAllRoleGroupsAsAdmin: {
    /**
     * 角色组查询条件
     */
    spec: RoleGroupAdminSpec
  }
  getRoleGroupUserAccounts: {
    spec: UserAccountRoleGroupAdminSpec
  }
}
