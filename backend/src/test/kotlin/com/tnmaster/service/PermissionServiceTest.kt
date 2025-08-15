package com.tnmaster.service

import io.github.truenine.composeserver.logger
import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import io.mockk.*

/**
 * PermissionService 单元测试
 *
 * @author TrueNine
 * @since 2025-01-10
 */
class PermissionServiceTest {

  private lateinit var enforcer: Enforcer
  private lateinit var permissionService: PermissionService

  @BeforeEach
  fun setUp() {
    enforcer = mockk()
    permissionService = PermissionService(enforcer)
  }

  @Test
  fun check_permission_success() {
    // Given
    val account = "testuser"
    val resource = "/api/user/profile"
    val action = "GET"

    every { enforcer.enforce(account, resource, action) } returns true

    // When
    val result = permissionService.checkPermission(account, resource, action)

    // Then
    assertTrue(result)
    verify { enforcer.enforce(account, resource, action) }
  }

  @Test
  fun check_permission_failure() {
    // Given
    val account = "testuser"
    val resource = "/api/admin/users"
    val action = "DELETE"

    every { enforcer.enforce(account, resource, action) } returns false

    // When
    val result = permissionService.checkPermission(account, resource, action)

    // Then
    assertFalse(result)
    verify { enforcer.enforce(account, resource, action) }
  }

  @Test
  fun check_permission_exception_handling() {
    // Given
    val account = "testuser"
    val resource = "/api/user/profile"
    val action = "GET"

    every { enforcer.enforce(account, resource, action) } throws RuntimeException("测试异常")

    // When
    val result = permissionService.checkPermission(account, resource, action)

    // Then
    assertFalse(result)
    verify { enforcer.enforce(account, resource, action) }
  }

  @Test
  fun check_role_success() {
    // Given
    val account = "testuser"
    val role = "admin"

    every { enforcer.hasRoleForUser(account, role) } returns true

    // When
    val result = permissionService.hasRole(account, role)

    // Then
    assertTrue(result)
    verify { enforcer.hasRoleForUser(account, role) }
  }

  @Test
  fun get_user_roles() {
    // Given
    val account = "testuser"
    val roles = listOf("user", "editor")

    every { enforcer.getRolesForUser(account) } returns roles

    // When
    val result = permissionService.getUserRoles(account)

    // Then
    assertAll(
      { assertTrue(result.contains("user")) },
      { assertTrue(result.contains("editor")) },
      { assertEquals(2, result.size) }
    )
    verify { enforcer.getRolesForUser(account) }
  }

  @Test
  fun get_user_permissions() {
    // Given
    val account = "testuser"
    val permissions = listOf(
      listOf("user", "read"),
      listOf("user", "write")
    )

    every { enforcer.getPermissionsForUser(account) } returns permissions

    // When
    val result = permissionService.getUserPermissions(account)

    // Then
    assertAll(
      { assertEquals(2, result.size) },
      { assertTrue(result.contains(listOf("user", "read"))) },
      { assertTrue(result.contains(listOf("user", "write"))) }
    )
    verify { enforcer.getPermissionsForUser(account) }
  }

  @Test
  fun add_user_role() {
    // Given
    val account = "testuser"
    val role = "admin"

    every { enforcer.addRoleForUser(account, role) } returns true

    // When
    val result = permissionService.addRoleForUser(account, role)

    // Then
    assertTrue(result)
    verify { enforcer.addRoleForUser(account, role) }
  }

  @Test
  fun remove_user_role() {
    // Given
    val account = "testuser"
    val role = "admin"

    every { enforcer.deleteRoleForUser(account, role) } returns true

    // When
    val result = permissionService.removeRoleForUser(account, role)

    // Then
    assertTrue(result)
    verify { enforcer.deleteRoleForUser(account, role) }
  }

  @Test
  fun add_policy() {
    // Given
    val role = "admin"
    val resource = "/api/admin/**"
    val action = "*"

    every { enforcer.addPolicy(role, resource, action) } returns true

    // When
    val result = permissionService.addPolicy(role, resource, action)

    // Then
    assertTrue(result)
    verify { enforcer.addPolicy(role, resource, action) }
  }

  @Test
  fun remove_policy() {
    // Given
    val role = "admin"
    val resource = "/api/admin/**"
    val action = "*"

    every { enforcer.removePolicy(role, resource, action) } returns true

    // When
    val result = permissionService.removePolicy(role, resource, action)

    // Then
    assertTrue(result)
    verify { enforcer.removePolicy(role, resource, action) }
  }

  @Test
  fun get_all_policies() {
    // Given
    val policies = listOf(
      listOf("admin", "/api/admin/**", "*"),
      listOf("user", "/api/user/**", "GET")
    )

    every { enforcer.policy } returns policies

    // When
    val result = permissionService.getAllPolicies()

    // Then
    assertAll(
      { assertEquals(2, result.size) },
      { assertTrue(result.contains(listOf("admin", "/api/admin/**", "*"))) },
      { assertTrue(result.contains(listOf("user", "/api/user/**", "GET"))) }
    )
  }

  @Test
  fun reload_policy_success() {
    // Given
    every { enforcer.loadPolicy() } returns Unit

    // When
    val result = permissionService.reloadPolicy()

    // Then
    assertTrue(result)
    verify { enforcer.loadPolicy() }
  }

  @Test
  fun reload_policy_exception_handling() {
    // Given
    every { enforcer.loadPolicy() } throws RuntimeException("测试异常")

    // When
    val result = permissionService.reloadPolicy()

    // Then
    assertFalse(result)
    verify { enforcer.loadPolicy() }
  }
}
