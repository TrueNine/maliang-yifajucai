package com.tnmaster.apis

import cn.dev33.satoken.annotation.SaCheckPermission
import com.tnmaster.dto.api.ApiAdminDto
import com.tnmaster.dto.api.ApiAdminSpec
import com.tnmaster.dto.api.ApiPatchPermissionsDto
import com.tnmaster.dto.menu.MenuAdminMenuVersionSpec
import com.tnmaster.dto.menu.MenuAdminPutDto
import com.tnmaster.dto.menu.MenuAdminSpec
import com.tnmaster.dto.menu.MenuView
import com.tnmaster.dto.permissions.PermissionsAdminPostDto
import com.tnmaster.dto.permissions.PermissionsAdminSpec
import com.tnmaster.dto.role.RoleAdminSpec
import com.tnmaster.dto.role.RolePermissionsAdminSpec
import com.tnmaster.dto.role.RolePermissionsPutDto
import com.tnmaster.dto.role.RolePostDto
import com.tnmaster.dto.role.RolePutDto
import com.tnmaster.dto.rolegroup.RoleGroupAdminSpec
import com.tnmaster.dto.rolegroup.RoleGroupPostDto
import com.tnmaster.dto.rolegroup.RoleGroupPutDto
import com.tnmaster.dto.rolegroup.RoleGroupRoleAdminSpec
import com.tnmaster.dto.rolegroup.RoleGroupRoleDeleteDto
import com.tnmaster.dto.rolegroup.RoleGroupRolePutDto
import com.tnmaster.dto.useraccount.UserAccountAdminView
import com.tnmaster.dto.useraccount.UserAccountRoleGroupAdminSpec
import com.tnmaster.dto.useraccount.UserAccountRoleGroupDeleteDto
import com.tnmaster.dto.useraccount.UserAccountRoleGroupPutDto
import com.tnmaster.entities.Menu
import com.tnmaster.entities.Permissions
import com.tnmaster.entities.Role
import com.tnmaster.entities.RoleGroup
import com.tnmaster.entities.UserAccount
import com.tnmaster.entities.pattern
import com.tnmaster.repositories.IApiRepo
import com.tnmaster.repositories.IMenuRepo
import com.tnmaster.repositories.IPermissionsRepo
import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import com.tnmaster.repositories.IUserAccountRepo
import io.github.truenine.composeserver.Pr
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.hasText
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.fetchPq
import io.github.truenine.composeserver.rds.toFetcher
import io.github.truenine.composeserver.security.crypto.base64
import io.github.truenine.composeserver.security.crypto.sha256
import io.github.truenine.composeserver.slf4j
import jakarta.validation.Valid
import org.babyfish.jimmer.client.meta.Api
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * # 访问控制 API
 *
 * 该 API 注重于 直接的 RBAC 权限直接管理，并非直接控制账号
 *
 * @author TrueNine
 * @since 2025-02-26
 */
@Api
@RestController
@SaCheckPermission("ADMIN")
@RequestMapping("v2/acl")
class AclV2Api(
  private val apiRepo: IApiRepo,
  private val roleGroupRepo: IRoleGroupRepo,
  private val menuRepo: IMenuRepo,
  private val roleRepo: IRoleRepo,
  private val permissionsRepo: IPermissionsRepo,
  private val userAccountRepo: IUserAccountRepo,
) {

  /**
   * ## 更新用户账户角色组（管理员权限） > 该接口仅限管理员权限调用，用于更新指定用户账户的角色组信息
   *
   * 接口路径：`PUT /user_account_role_groups` 权限要求：管理员权限
   *
   * @param dto 用户账户角色组更新DTO，包含以下字段：
   * @return 更新后的用户账户实体
   */
  @Api
  @PutMapping("user_account_role_groups")
  fun putUserAccountRoleGroupsAsAdmin(@RequestBody dto: UserAccountRoleGroupPutDto): UserAccount {
    return userAccountRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /**
   * ## 删除用户账户角色组 > 管理员权限操作，根据条件删除用户账户角色组
   *
   * 该接口用于管理员根据指定条件删除用户账户角色组，操作成功后返回更新后的用户账户信息。
   *
   * @param dto 删除条件DTO，包含删除用户账户角色组所需的条件信息
   * @return 更新后的用户账户信息
   */
  @Api
  @DeleteMapping("user_account_role_groups")
  fun deleteUserAccountRoleGroupAsAdmin(
    @RequestBody dto: UserAccountRoleGroupDeleteDto,
  ): UserAccount {
    return userAccountRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /**
   * ## 更新角色权限 > 根据传入的角色权限信息，更新系统中的角色权限配置
   *
   * 该接口用于更新指定角色的权限信息，系统会根据传入的权限列表重新配置角色的权限。 更新操作会覆盖原有的权限配置，请确保传入的权限列表是完整的。
   *
   * @param dto 角色权限更新数据传输对象，包含角色ID和新的权限列表
   * @return 更新后的角色实体对象，包含最新的权限配置信息
   */
  @Api
  @PostMapping("role_permissions")
  fun postRolePermissions(@RequestBody dto: RolePermissionsPutDto): Role {
    return roleRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /**
   * ## 删除角色下的权限 > 管理员权限操作，用于删除指定角色的权限
   *
   * 该接口需要`ADMIN`权限，通过传入的角色权限DTO对象，更新角色权限信息。
   *
   * @param dto 角色权限更新DTO对象，包含需要更新的角色ID和权限列表
   * @return 更新后的角色实体对象
   */
  @Api
  @DeleteMapping("role_permissions")
  fun deleteRolePermissions(@RequestBody dto: RolePermissionsPutDto): Role {
    return roleRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /** ## 获取所有权限列表 */
  @Api
  @GetMapping("permissions")
  fun getAllPermissionsAsAdmin(spec: PermissionsAdminSpec): IPage<Permissions> {
    return permissionsRepo
      .sql
      .createQuery(Permissions::class) {
        where(spec)
        select(table)
      }
      .fetchPq(spec)
  }

  /** ## 保存新的 API */
  @Api
  @ACID
  @PostMapping("api")
  fun postApiAsAdmin(@RequestBody dto: ApiAdminDto): com.tnmaster.entities.Api {
    return apiRepo.save(dto, ApiAdminDto::class.toFetcher())
  }

  /** ## 修改 API */
  @Api
  @ACID
  @PutMapping("api")
  fun putApiAsAdmin(@RequestBody dto: ApiAdminDto): com.tnmaster.entities.Api {
    return apiRepo
      .saveCommand(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE) {}
      .execute()
      .modifiedEntity
  }

  /** ## 创建角色组 */
  @Api
  @SaCheckPermission("ADMIN")
  @PostMapping("role_group")
  fun postRoleGroupAsAdmin(@RequestBody dto: RoleGroupPostDto): RoleGroup {
    return roleGroupRepo.save(dto, SaveMode.INSERT_ONLY, AssociatedSaveMode.REPLACE)
  }

  /** ## 获取所有 API */
  @Api
  @GetMapping("apis")
  fun getAllApisAsAdmin(@Valid spec: ApiAdminSpec): Pr<com.tnmaster.entities.Api> {
    return apiRepo.findAllBySpec(spec, spec)
  }

  /** ## 修改 API 的权限 */
  @Api
  @PatchMapping("api")
  fun patchApiPermissions(dto: ApiPatchPermissionsDto): com.tnmaster.entities.Api {
    return apiRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /** ## 根据 id 删除角色 */
  @Api
  @DeleteMapping("role")
  fun deleteRoleByIdAsAdmin(@RequestParam id: RefId) {
    roleRepo.deleteById(id)
  }

  /** ## 根据 id 删除角色组 */
  @Api
  @SaCheckPermission("ADMIN")
  @DeleteMapping("role_group")
  fun deleteRoleGroupByIdAsAdmin(@RequestParam id: RefId) {
    roleGroupRepo.deleteById(id)
  }

  /** ## 根据 id 删除权限 */
  @Api
  @SaCheckPermission("ADMIN")
  @DeleteMapping("permissions")
  fun deletePermissionsByIdAsAdmin(@RequestParam id: RefId) {
    permissionsRepo.deleteById(id)
  }

  /**
   * ## 查询菜单
   *
   * @param spec 查询参数
   */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("menus")
  fun getMenus(spec: MenuAdminSpec): IPage<Menu> {
    return menuRepo.sql
      .createQuery(Menu::class) {
        where(spec)
        orderBy(table.pattern)
        select(table.fetch(MenuView::class.toFetcher()))
      }
      .fetchPq(spec)
  }

  /** ## 获取所有角色 */
  @Api
  @SaCheckPermission("ADMIN")
  @GetMapping("roles")
  fun getRolesAsAdmin(spec: RoleAdminSpec): IPage<Role> {
    return roleRepo.sql
      .createQuery(Role::class) {
        where(spec)
        select(table)
      }
      .fetchPq(spec)
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
   * @param dtos 菜单更新DTO列表，包含菜单的详细信息
   * @return 更新后的菜单实体列表
   * @throws IllegalArgumentException 如果传入的菜单数据中存在空白的pattern字段
   */
  @Api
  @ACID
  @PutMapping("menus")
  fun putMenusAsAdmin(@RequestBody dtos: List<MenuAdminPutDto>): List<Menu> {
    // 校验所有菜单的pattern字段不为空
    require(dtos.all { it.pattern.isNotBlank() }) { "内部有菜单匹配项为空" }

    // 清空现有菜单数据
    menuRepo.deleteAll()

    // 批量插入新的菜单数据，并返回更新后的实体
    val menus = menuRepo.saveInputs(dtos)

    // 记录操作日志
    log.trace("save menus: {}", menus)
    return menus
  }

  /**
   * ## 创建或者更新菜单
   *
   * @param dto 保存的菜单信息
   */
  @Api
  @ACID
  @PostMapping("menu")
  fun postMenuByPatternAsAdmin(@RequestBody dto: MenuAdminPutDto): Menu {
    require(dto.pattern.isNotBlank()) { "菜单匹配项为空" }
    val menu = menuRepo.save(dto)
    log.trace("save menu: {}", menu)
    return menu
  }

  /**
   * ## 客户端传入 menu 以服务器进行校验
   *
   * @param menus 客户端传入的菜单路径
   */
  @Api
  @GetMapping("client_menu_version")
  fun getClientMenuVersion(@RequestParam menus: List<String>): String {
    if (menus.isEmpty()) return ""
    return menus.sorted().joinToString(separator = "").base64().sha256
  }

  /**
   * ## 创建角色
   *
   * @param dto 角色信息
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PostMapping("role")
  fun postRoleAsAdmin(@RequestBody dto: RolePostDto): Role {
    return roleRepo.saveCommand(dto).execute().modifiedEntity
  }

  /**
   * ## 更新角色信息
   *
   * @param dto 更新的角色信息
   */
  @Api
  @SaCheckPermission("ADMIN")
  @PutMapping("role")
  fun putRoleAsAdmin(@RequestBody dto: RolePutDto): Role {
    return roleRepo.saveCommand(dto, SaveMode.UPDATE_ONLY).execute().modifiedEntity
  }

  /** ## 更新角色组信息 */
  @Api
  @SaCheckPermission("ADMIN")
  @PutMapping("role_group")
  fun putRoleGroupAsAdmin(@RequestBody dto: RoleGroupPutDto): RoleGroup {
    return roleGroupRepo.saveCommand(dto) {}.execute().modifiedEntity
  }

  /** ## 创建权限 */
  @Api
  @PostMapping("permissions")
  fun postPermissions(dto: PermissionsAdminPostDto): Permissions {
    return permissionsRepo.save(dto)
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
  @Api
  @GetMapping("server_menu_version")
  fun getServerMenuVersion(spec: MenuAdminMenuVersionSpec): String {
    val resultStr =
      roleGroupRepo
        .sql
        .createQuery(Menu::class) {
          where(spec)
          orderBy(table.pattern)
          select(table.pattern)
        }
        .execute()
        .joinToString(separator = "") { it }
    log.info("getServerMenuVersion resultStr: {}", resultStr)
    return if (resultStr.hasText()) {
      resultStr.base64().sha256
    } else ""
  }

  /** ## 查询 角色下的权限 */
  @Api
  @GetMapping("role_permissions")
  fun getALlRolePermissionsAsAdmin(spec: RolePermissionsAdminSpec): IPage<Permissions> {
    return roleRepo.sql
      .createQuery(Role::class) {
        where(spec)
        select(table.joinList(Role::permissions))
      }
      .fetchPq(spec)
  }

  /** ## 查询角色组其下的角色 */
  @Api
  @GetMapping("role_group_roles")
  fun getAllRoleGroupRolesAsAdmin(spec: RoleGroupRoleAdminSpec): IPage<Role> {
    return roleGroupRepo
      .sql
      .createQuery(RoleGroup::class) {
        where(spec)
        select(table.joinList(RoleGroup::roles))
      }
      .fetchPq(spec)
  }

  /**
   * ## 更新角色组与角色的关联关系 > 根据传入的DTO对象，更新角色组与角色的关联关系
   *
   * 该接口用于批量更新角色组与角色的关联关系，支持添加或移除角色组中的角色。 接口采用幂等设计，多次调用结果一致。
   *
   * @param dto 角色组角色更新DTO对象，包含角色组ID和待更新的角色ID列表
   * @return 更新后的角色组实体对象
   */
  @Api
  @PutMapping("role_group_roles")
  fun putRoleGroupRoles(@RequestBody dto: RoleGroupRolePutDto): RoleGroup {
    return roleGroupRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE)
  }

  /**
   * ## 删除角色组关联角色 > 根据传入的 DTO 删除角色组与角色的关联关系
   *
   * 该接口用于移除角色组中指定的角色关联，操作成功后返回更新后的角色组信息
   *
   * @param dto 角色组角色删除数据传输对象，包含要删除的角色组ID和角色ID列表
   * @return 更新后的角色组实体
   */
  @Api
  @DeleteMapping("role_group_roles")
  fun deleteRoleGroupRoles(@RequestBody dto: RoleGroupRoleDeleteDto): RoleGroup {
    return roleGroupRepo.save(dto, SaveMode.UPDATE_ONLY, AssociatedSaveMode.REPLACE)
  }

  /**
   * ## 管理员获取所有角色组
   *
   * @param spec 角色组查询条件
   */
  @Api
  @GetMapping("role_groups")
  fun getAllRoleGroupsAsAdmin(spec: RoleGroupAdminSpec): IPage<RoleGroup> {
    return roleGroupRepo
      .sql
      .createQuery(RoleGroup::class) {
        where(spec)
        select(table)
      }
      .fetchPq(spec)
  }

  @Api
  @GetMapping("role_group_user_accounts")
  fun getRoleGroupUserAccounts(spec: UserAccountRoleGroupAdminSpec): IPage<UserAccountAdminView> {
    return userAccountRepo
      .sql
      .createQuery(UserAccount::class) {
        where(spec)
        select(table.fetch(UserAccountAdminView::class))
      }
      .fetchPq(spec)
  }

  companion object {
    @JvmStatic
    private val log = slf4j<AclV2Api>()
  }
}
