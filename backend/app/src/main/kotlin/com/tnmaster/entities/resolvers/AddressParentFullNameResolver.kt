package com.tnmaster.entities.resolvers

import com.tnmaster.entities.Address
import com.tnmaster.entities.code
import com.tnmaster.entities.id
import com.tnmaster.entities.name
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.data.extract.domain.CnDistrictCode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.KTransientResolver
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.springframework.stereotype.Component

@Component
class AddressParentFullNameResolver(private val sql: KSqlClient) : KTransientResolver<RefId, String?> {
  override fun resolve(ids: Collection<RefId>): Map<RefId, String?> {
    if (ids.isEmpty()) return emptyMap()
    val idCodeNames =
      sql
        .createQuery(Address::class) {
          where(table.id valueIn ids)
          select(table.id, table.code, table.name)
        }
        .execute()

    val idCodes =
      idCodeNames.associate { (id, code) ->
        val codes = mutableListOf<String>()
        var districtCode: CnDistrictCode? = CnDistrictCode(code)
        do {
          codes += districtCode!!.code
          districtCode = districtCode.back()
        } while (districtCode != null)
        id to (codes.sorted())
      }
    val codes = idCodes.map { (_, value) -> value }.flatten().toSet()
    val allCodeAndNames =
      sql
        .createQuery(Address::class) {
          where(table.code valueIn codes)
          orderBy(table.code)
          select(table.code, table.name)
        }
        .execute()

    return idCodes.mapValues { (_, codes) ->
      allCodeAndNames.filter { (code) -> code in codes }.sortedBy { (code) -> code.length }.joinToString("") { (_, name) -> name }
    }
  }
}
