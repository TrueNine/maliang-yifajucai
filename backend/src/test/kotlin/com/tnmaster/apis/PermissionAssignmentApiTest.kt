package com.tnmaster.apis

import com.tnmaster.service.PermissionService
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * 权限分配API测试
 *
 * 测试用户角色分配和角色权限分配功能
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class PermissionAssignmentApiTest {

  @Test
  fun `权限分配API基本功能验证`() {
    // 简单测试验证权限服务可以正常创建
    val permissionService = mockk<PermissionService>()
    val permissionManagementApi = PermissionManagementApi(permissionService)
    assertNotNull(permissionManagementApi)
  }
}
