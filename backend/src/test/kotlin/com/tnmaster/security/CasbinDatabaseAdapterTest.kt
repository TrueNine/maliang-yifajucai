package com.tnmaster.security

import com.tnmaster.repositories.IRoleGroupRepo
import com.tnmaster.repositories.IRoleRepo
import com.tnmaster.repositories.IUserAccountRepo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.casbin.jcasbin.model.Model
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * CasbinDatabaseAdapter 测试
 *
 * 测试策略存储和加载功能
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class CasbinDatabaseAdapterTest {

  private lateinit var userAccountRepo: IUserAccountRepo
  private lateinit var roleGroupRepo: IRoleGroupRepo
  private lateinit var roleRepo: IRoleRepo
  private lateinit var adapter: CasbinDatabaseAdapter
  private lateinit var model: Model

  @BeforeEach
  fun setUp() {
    userAccountRepo = mockk()
    roleGroupRepo = mockk()
    roleRepo = mockk()
    adapter = CasbinDatabaseAdapter(userAccountRepo, roleGroupRepo, roleRepo)
    model = mockk(relaxed = true)
  }

  @Nested
  inner class PolicyLoadingTests {

    @Test
    fun load_policy_success_with_all_policy_types() {
      // Given
      val userRoleGroups = listOf(
        "admin" to "admin_group",
        "user1" to "user_group",
        "user2" to "user_group"
      )
      val roleGroupRoles = listOf(
        "admin_group" to "admin",
        "user_group" to "user",
        "user_group" to "editor"
      )
      val rolePermissions = listOf(
        "admin" to "system:admin",
        "user" to "user:read",
        "editor" to "article:write"
      )

      every { userAccountRepo.findAllUserRoleGroups() } returns userRoleGroups
      every { roleGroupRepo.findAllRoleGroupRoles() } returns roleGroupRoles
      every { roleRepo.findAllRolePermissions() } returns rolePermissions

      // When
      adapter.loadPolicy(model)

      // Then
      verify { userAccountRepo.findAllUserRoleGroups() }
      verify { roleGroupRepo.findAllRoleGroupRoles() }
      verify { roleRepo.findAllRolePermissions() }

      // 验证用户-角色组关系被添加
      verify { model.addPolicy("g", "g", listOf("admin", "admin_group")) }
      verify { model.addPolicy("g", "g", listOf("user1", "user_group")) }
      verify { model.addPolicy("g", "g", listOf("user2", "user_group")) }

      // 验证角色组-角色关系被添加
      verify { model.addPolicy("g", "g2", listOf("admin_group", "admin")) }
      verify { model.addPolicy("g", "g2", listOf("user_group", "user")) }
      verify { model.addPolicy("g", "g2", listOf("user_group", "editor")) }

      // 验证角色-权限关系被添加
      verify { model.addPolicy("p", "p", listOf("admin", "system:admin", "allow")) }
      verify { model.addPolicy("p", "p", listOf("user", "user:read", "allow")) }
      verify { model.addPolicy("p", "p", listOf("editor", "article:write", "allow")) }
    }

    @Test
    fun load_policy_success_with_empty_data() {
      // Given
      every { userAccountRepo.findAllUserRoleGroups() } returns emptyList()
      every { roleGroupRepo.findAllRoleGroupRoles() } returns emptyList()
      every { roleRepo.findAllRolePermissions() } returns emptyList()

      // When
      adapter.loadPolicy(model)

      // Then
      verify { userAccountRepo.findAllUserRoleGroups() }
      verify { roleGroupRepo.findAllRoleGroupRoles() }
      verify { roleRepo.findAllRolePermissions() }

      // 验证没有策略被添加（除了可能的初始化调用）
      verify(exactly = 0) { model.addPolicy("g", "g", any()) }
      verify(exactly = 0) { model.addPolicy("g", "g2", any()) }
      verify(exactly = 0) { model.addPolicy("p", "p", any()) }
    }

    @Test
    fun load_policy_failure_database_exception() {
      // Given
      every { userAccountRepo.findAllUserRoleGroups() } throws RuntimeException("数据库连接失败")

      // When & Then
      try {
        adapter.loadPolicy(model)
      } catch (e: RuntimeException) {
        // 预期会抛出异常
      }

      verify { userAccountRepo.findAllUserRoleGroups() }
    }
  }

  @Nested
  inner class PolicySavingTests {

    @Test
    fun save_policy_method_not_implemented_but_no_exception() {
      // When & Then - 应该不抛出异常，只是记录警告
      adapter.savePolicy(model)
    }

    @Test
    fun add_policy_method_not_implemented_but_no_exception() {
      // When & Then - 应该不抛出异常，只是记录警告
      adapter.addPolicy("p", "p", listOf("user", "resource", "action"))
    }

    @Test
    fun remove_policy_method_not_implemented_but_no_exception() {
      // When & Then - 应该不抛出异常，只是记录警告
      adapter.removePolicy("p", "p", listOf("user", "resource", "action"))
    }

    @Test
    fun remove_filtered_policy_method_not_implemented_but_no_exception() {
      // When & Then - 应该不抛出异常，只是记录警告
      adapter.removeFilteredPolicy("p", "p", 0, "user")
    }
  }

  @Nested
  inner class PartialLoadingTests {

    @Test
    fun load_policy_with_only_user_role_group_data() {
      // Given
      val userRoleGroups = listOf("admin" to "admin_group")
      every { userAccountRepo.findAllUserRoleGroups() } returns userRoleGroups
      every { roleGroupRepo.findAllRoleGroupRoles() } returns emptyList()
      every { roleRepo.findAllRolePermissions() } returns emptyList()

      // When
      adapter.loadPolicy(model)

      // Then
      verify { model.addPolicy("g", "g", listOf("admin", "admin_group")) }
    }

    @Test
    fun load_policy_with_only_role_group_role_data() {
      // Given
      val roleGroupRoles = listOf("admin_group" to "admin")
      every { userAccountRepo.findAllUserRoleGroups() } returns emptyList()
      every { roleGroupRepo.findAllRoleGroupRoles() } returns roleGroupRoles
      every { roleRepo.findAllRolePermissions() } returns emptyList()

      // When
      adapter.loadPolicy(model)

      // Then
      verify { model.addPolicy("g", "g2", listOf("admin_group", "admin")) }
    }

    @Test
    fun load_policy_with_only_role_permission_data() {
      // Given
      val rolePermissions = listOf("admin" to "system:admin")
      every { userAccountRepo.findAllUserRoleGroups() } returns emptyList()
      every { roleGroupRepo.findAllRoleGroupRoles() } returns emptyList()
      every { roleRepo.findAllRolePermissions() } returns rolePermissions

      // When
      adapter.loadPolicy(model)

      // Then
      verify { model.addPolicy("p", "p", listOf("admin", "system:admin", "allow")) }
    }
  }

  @Test
  fun `适配器能正常创建`() {
    // 简单测试验证适配器可以正常创建
    assertNotNull(adapter)
  }
}
