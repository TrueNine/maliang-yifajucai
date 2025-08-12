package com.tnmaster.application.repositories

import com.tnmaster.entities.Address
import com.tnmaster.entities.by
import com.tnmaster.entities.code
import com.tnmaster.repositories.IAddressRepo
import io.github.truenine.composeserver.testtoolkit.RDBRollback
import io.github.truenine.composeserver.testtoolkit.testcontainers.IDatabasePostgresqlContainer
import io.github.truenine.composeserver.testtoolkit.testcontainers.IOssMinioContainer
import io.github.truenine.composeserver.toId
import jakarta.annotation.Resource
import org.babyfish.jimmer.sql.kt.ast.expression.ne
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.*

@SpringBootTest
@Transactional
@RDBRollback
class IAddressRepoTest : IDatabasePostgresqlContainer, IOssMinioContainer {

  @Resource
  private lateinit var addressRepo: IAddressRepo

  private lateinit var rootAddress: Address
  private lateinit var provinceAddress: Address
  private lateinit var cityAddress: Address
  private lateinit var districtAddress: Address

  @BeforeEach
  fun setup() {
    rootAddress = addressRepo.findRootAddressNode()!!

    // 清除测试数据
    addressRepo.sql.createDelete(Address::class) {
      where(table.code ne rootAddress.code)
    }.execute()

    // 获取根节点
    rootAddress = addressRepo.findRootAddressNode()!!

    // 省级地址 - 北京市
    provinceAddress = Address {
      code = "110000"
      name = "北京市"
      level = 1
      rpi = rootAddress.id
      leaf = false
    }
    provinceAddress = addressRepo.save(provinceAddress)

    // 市级地址 - 北京市
    cityAddress = Address {
      code = "110100"
      name = "北京市"
      level = 2
      rpi = provinceAddress.id
      leaf = false
    }
    cityAddress = addressRepo.save(cityAddress)

    // 区级地址 - 东城区
    districtAddress = Address {
      code = "110101"
      name = "东城区"
      level = 3
      rpi = cityAddress.id
      leaf = true
    }
    districtAddress = addressRepo.save(districtAddress)
  }

  @Nested
  inner class FindIdByCodeFunctionGroup {

    @Test
    fun normal_existing_address_code_should_return_correct_id() {
      // when
      val id = addressRepo.findIdByCode("110101")

      // then
      assertNotNull(id)
      assertEquals(districtAddress.id, id)
    }

    @Test
    fun abnormal_nonexistent_address_code_should_return_null() {
      // when
      val id = addressRepo.findIdByCode("999999")

      // then
      assertNull(id)
    }
  }

  @Nested
  inner class FindAllByCodeInFunctionGroup {

    @Test
    fun normal_existing_address_code_list_should_return_correct_address_view_collection() {
      // when
      val addresses = addressRepo.findAllByCodeIn(listOf("110000", "110101"))

      // then
      assertEquals(2, addresses.size)
      assertTrue(addresses.any { it.code == "110000" })
      assertTrue(addresses.any { it.code == "110101" })
    }

    @Test
    fun normal_empty_list_should_return_empty_collection() {
      // when
      val addresses = addressRepo.findAllByCodeIn(emptyList())

      // then
      assertTrue(addresses.isEmpty())
    }

    @Test
    fun abnormal_nonexistent_codes_should_return_only_existing_code_addresses() {
      // when
      val addresses = addressRepo.findAllByCodeIn(listOf("110000", "999999"))

      // then
      assertEquals(1, addresses.size)
      assertEquals("110000", addresses[0].code)
    }
  }

  @Nested
  inner class ExistsByCodeFunctionGroup {

    @Test
    fun normal_existing_address_code_should_return_true() {
      // when
      val exists = addressRepo.existsByCode("110101")

      // then
      assertTrue(exists)
    }

    @Test
    fun abnormal_nonexistent_address_code_should_return_false() {
      val exists = addressRepo.existsByCode("9999")
      assertFalse(exists)
    }
  }

  @Nested
  inner class FindFirstByCodeOrNullFunctionGroup {

    @Test
    fun normal_existing_address_code_should_return_correct_address() {
      // when
      val address = addressRepo.findFirstByCodeOrNull("110101")

      // then
      assertNotNull(address)
      assertEquals("110101", address.code)
      assertEquals("东城区", address.name)
    }

    @Test
    fun normal_using_fetcher_should_return_address_with_specified_properties() {
      // given
      val fetcher = newFetcher(Address::class).by {
        allScalarFields()
        parentAddress {
          allScalarFields()
        }
      }

      // when
      val address = addressRepo.findFirstByCodeOrNull("110101", fetcher)

      // then
      assertNotNull(address)
      assertEquals("110101", address.code)
      assertNotNull(address.parentAddress)
      assertEquals("110100", address.parentAddress?.code)
    }

    @Test
    fun abnormal_nonexistent_address_code_should_return_null() {
      // when
      val address = addressRepo.findFirstByCodeOrNull("999999")

      // then
      assertNull(address)
    }
  }

  @Nested
  inner class FindDirectChildrenByCodeFunctionGroup {

    @Test
    fun normal_address_code_with_children_should_return_all_children() {
      // when
      val children = addressRepo.findDirectChildrenByCode("110000")

      // then
      assertEquals(1, children.size)
      assertEquals("110100", children[0].code)
    }

    @Test
    fun normal_address_code_without_children_should_return_empty_list() {
      // when
      val children = addressRepo.findDirectChildrenByCode("110101")

      // then
      assertTrue(children.isEmpty())
    }

    @Test
    fun abnormal_nonexistent_address_code_should_return_empty_list() {
      // when
      val children = addressRepo.findDirectChildrenByCode("999999")

      // then
      assertTrue(children.isEmpty())
    }

    @Test
    fun normal_using_fetcher_should_return_list_of_children_with_specified_properties() {
      // given
      val fetcher = newFetcher(Address::class).by {
        allScalarFields()
        parentAddress {
          allScalarFields()
        }
      }

      // when
      val children = addressRepo.findDirectChildrenByCode("110000", fetcher)

      // then
      assertEquals(1, children.size)
      assertEquals("110100", children[0].code)
      assertNotNull(children[0].parentAddress)
      assertEquals("110000", children[0].parentAddress?.code)
    }
  }

  @Nested
  inner class FindRootAddressNodeFunctionGroup {

    @Test
    fun normal_default_root_id_should_return_root_node() {
      // when
      val root = addressRepo.findRootAddressNode()

      // then
      assertNotNull(root)
      assertEquals("000000000000", root.code)
    }

    @Test
    fun normal_specified_root_id_should_return_specified_node() {
      // when
      val root = addressRepo.findRootAddressNode(provinceAddress.id)

      // then
      assertNotNull(root)
      assertEquals(provinceAddress.id, root.id)
      assertEquals("110000", root.code)
    }

    @Test
    fun abnormal_nonexistent_root_id_should_return_null() {
      // when
      val root = addressRepo.findRootAddressNode((999L).toId()!!)

      // then
      assertNull(root)
    }
  }

  @Nested
  inner class FindAllFullPathByCodesInFunctionGroup {

    @Test
    fun normal_existing_address_code_list_should_return_correct_address_view_collection() {
      // when
      val addresses = addressRepo.findAllFullPathByCodesIn(listOf("110000", "110101"))

      // then
      assertEquals(2, addresses.size)
      assertTrue(addresses.any { it.code == "110000" })
      assertTrue(addresses.any { it.code == "110101" })
    }

    @Test
    fun normal_empty_list_should_return_empty_collection() {
      // when
      val addresses = addressRepo.findAllFullPathByCodesIn(emptyList())

      // then
      assertTrue(addresses.isEmpty())
    }

    @Test
    fun abnormal_nonexistent_codes_should_return_only_existing_code_addresses() {
      // when
      val addresses = addressRepo.findAllFullPathByCodesIn(listOf("110000", "999999"))

      // then
      assertEquals(1, addresses.size)
      assertEquals("110000", addresses[0].code)
    }
  }
}
