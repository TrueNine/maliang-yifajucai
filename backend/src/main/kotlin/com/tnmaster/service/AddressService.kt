package com.tnmaster.service

import com.tnmaster.entities.Address
import com.tnmaster.entities.code
import com.tnmaster.entities.id
import com.tnmaster.entities.rpi
import com.tnmaster.repositories.IAddressRepo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.data.extract.domain.CnDistrictCode
import io.github.truenine.composeserver.data.extract.service.ILazyAddressService
import io.github.truenine.composeserver.rds.annotations.ACID
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueNotIn
import org.springframework.stereotype.Service

@Service
class AddressService(
  private val addressRepo: IAddressRepo,
  private val lazyService: ILazyAddressService,
) {

  @ACID
  fun initProvince(): List<Address> {
    return fetchRootAddressNode()?.let { root ->
      addressRepo
        .sql
        .createDelete(Address::class) { where(table.id valueNotIn listOf(root.id)) }
        .execute()
      lazyService
        .fetchAllProvinces()
        .map { addr ->
          Address {
            rpi = root.id
            name = addr.name
            code = addr.code.code
            yearVersion = addr.yearVersion.toIntOrNull()
            level = 1
          }
        }
        .let { addressRepo.saveAll(it) }
    }
      ?: emptyList()
  }

  @ACID
  fun fetchDirectChildrenByCode(code: String): List<Address> {
    val c = CnDistrictCode(code)
    if (c.empty) return emptyList()
    val resultList = addressRepo.findDirectChildrenByCode(c.code)
    if (resultList.isNotEmpty()) {
      return resultList.sortedBy { it.code }
    }
    val allSavedEntities = mutableListOf<Address>()
    lazyService.traverseChildrenRecursive(code, c.level + 1) { children, _, parentDistrict ->
      val parentId = parentDistrict?.code?.code?.let { addressRepo.findIdByCode(it) }
      val saveEntities =
        children.map {
          Address {
            rpi = parentId
            name = it.name
            this.code = it.code.code
            level = it.level
            yearVersion = it.yearVersion.toIntOrNull()
          }
        }
      allSavedEntities += saveEntities
      parentDistrict?.level == c.level + 1
    }
    val allSaveEntities = addressRepo.saveAll(allSavedEntities)
    return allSaveEntities
      .filter { it.level == c.level + 1 && it.code.startsWith(c.code) }
      .distinctBy { it.code }
      .sortedBy { it.code }
  }

  /** ## 查询直接子级 */
  fun fetchDirectChildren(parentId: RefId?, fetcher: Fetcher<Address>? = null): List<Address> {
    if (parentId == null) {
      return emptyList()
    }
    return addressRepo
      .sql
      .createQuery(Address::class) {
        where(table.rpi eq parentId)
        orderBy(table.code)
        select(table.fetch(fetcher))
      }
      .execute()
  }

  /** ## 查询所有省份 */
  @ACID
  fun fetchProvince(): List<Address> {
    val root = fetchRootAddressNode() ?: return emptyList()
    val provinces = fetchDirectChildren(root.rpi)
    if (provinces.isNotEmpty()) {
      return provinces
    }
    return initProvince()
  }

  /** ## 获取地址的根节点 */
  fun fetchRootAddressNode(): Address? {
    return addressRepo.findRootAddressNode()
  }
}
