package com.tnmaster.repositories

import com.tnmaster.dto.address.AddressFullPathView
import com.tnmaster.entities.*
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.consts.IDbNames.Rbac
import io.github.truenine.composeserver.data.extract.domain.CnDistrictCode
import io.github.truenine.composeserver.rds.IRepo
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.`valueIn?`
import org.babyfish.jimmer.sql.kt.exists
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Primary
@Repository
interface IAddressRepo : IRepo<Address, RefId> {
  fun findIdByCode(code: String): RefId? {
    val c = CnDistrictCode(code)
    return sql.createQuery(Address::class) {
      where(
        table.code eq c.code,
        table.level eq c.level,
      )
      select(table.id)
    }.execute().firstOrNull()
  }

  fun findAllByCodeIn(codes: List<String>): List<AddressFullPathView> {
    return sql
      .createQuery(Address::class) {
        where(table.code `valueIn?` codes)
        select(table.fetch(AddressFullPathView::class))
      }
      .execute()
  }

  fun existsByCode(code: String): Boolean {
    return sql.exists(Address::class) {
      where(
        table.code eq CnDistrictCode(code).code
      )
    }
  }

  fun findFirstByCodeOrNull(code: String, fetcher: Fetcher<Address>? = null): Address? {
    return sql
      .createQuery(Address::class) {
        where(table.code eq code)
        select(table.fetch(fetcher))
      }
      .execute()
      .firstOrNull()
  }

  /**
   * ## 查询地址的直接子集
   *
   * @param code 地址编码
   * @param fetcher 节点的子集的获取器
   */
  fun findDirectChildrenByCode(code: String, fetcher: Fetcher<Address>? = null): List<Address> {
    return findFirstByCodeOrNull(code)?.let {
      sql
        .createQuery(Address::class) {
          where(table.rpi eq it.id)
          orderBy(table.code)
          select(table.fetch(fetcher))
        }
        .execute()
    } ?: emptyList()
  }

  fun findRootAddressNode(rootId: RefId = Rbac.ROOT_ID): Address? {
    return findById(rootId).getOrNull()
  }

  fun findAllFullPathByCodesIn(codes: List<String>): List<AddressFullPathView> {
    return sql
      .createQuery(Address::class) {
        where(table.code `valueIn?` codes)
        select(table.fetch(AddressFullPathView::class))
      }
      .execute()
  }
}
