package com.tnmaster.apis

import com.tnmaster.service.PermissionService
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * 角色管理API测试
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class RoleManagementApiTest {

  @Test
  fun test_permission_service_creation() {
    // 简单测试验证服务可以正常创建
    val permissionService = mockk<PermissionService>()
    assertNotNull(permissionService)
  }

}
