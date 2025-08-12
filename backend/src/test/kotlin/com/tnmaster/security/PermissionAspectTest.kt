package com.tnmaster.security

import com.tnmaster.service.PermissionService
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * 权限检查服务测试
 *
 * @author TrueNine
 * @since 2025-01-11
 */
class PermissionAspectTest {

  @Test
  fun create_permission_aspect() {
    // 简单测试验证服务可以正常创建
    val permissionService = mockk<PermissionService>()
    val permissionAspect = PermissionAspect(permissionService)
    assertNotNull(permissionAspect)
  }
}
