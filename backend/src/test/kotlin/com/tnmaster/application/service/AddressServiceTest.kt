package com.tnmaster.application.service

import com.tnmaster.entities.Address
import com.tnmaster.repositories.IAddressRepo
import com.tnmaster.service.AddressService
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.consts.IDbNames.Rbac
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import jakarta.annotation.Resource
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class AddressServiceTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  lateinit var addressService: AddressService

  @Resource
  lateinit var addressRepo: IAddressRepo

  @Nested
  inner class FetchRootAddressNodeFunctionGroup {

    @Test
    @RDBRollback
    fun normal_case_should_return_root_address_node() {
      // When - 调用服务方法
      val result = addressService.fetchRootAddressNode()

      // Then - 验证根节点的基本属性
      assertNotNull(result, "根地址节点不应为空")
      assertEquals(Rbac.ROOT_ID, result.id, "根节点ID应该是0")
      assertEquals(0, result.level, "根节点级别应该是0")
      assertEquals("000000000000", result.code, "根节点代码应该是000000000000")

      // 根据Flyway初始化脚本，根节点的name初始化为空字符串
      // 这是符合预期的，因为根节点代表整个地址树的根，name为空是合理的
    }

    @Test
    @RDBRollback
    fun when_root_node_exists_but_info_is_empty_should_return_that_node() {
      // Given - 根节点存在但信息可能为空（这是实际情况）
      // 不需要额外操作，因为数据库中已经存在根节点

      // When
      val result = addressService.fetchRootAddressNode()

      // Then
      assertNotNull(result, "根地址节点不应为空")
      assertEquals(Rbac.ROOT_ID, result.id)
      assertEquals(0, result.level)
      // 注意：name可能为空字符串，这是正常的
    }
  }
}
