package com.tnmaster.entities.resolvers

import com.tnmaster.entities.*
import io.github.truenine.composeserver.RefId
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.KTransientResolver
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullExpression
import org.babyfish.jimmer.sql.kt.ast.expression.and
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.ast.table.KWeakJoin
import org.springframework.stereotype.Component

@Component
class EnterpriseAddressResolver(
  private val sql: KSqlClient,
) : KTransientResolver<RefId, RefId?> {
  class EnterpriseAddressJoin : KWeakJoin<Enterprise, Address>() {
    override fun on(source: KNonNullTable<Enterprise>, target: KNonNullTable<Address>): KNonNullExpression<Boolean>? = and(source.addressCode eq target.code)
  }

  override fun resolve(ids: Collection<RefId>): Map<RefId, RefId?> {
    if (ids.isEmpty()) return emptyMap()
    return sql
      .createQuery(Enterprise::class) {
        where(table.id valueIn ids)
        select(table.id, table.asTableEx().weakJoin(EnterpriseAddressJoin::class).id)
      }
      .execute()
      .associate { (id, addrId) -> id to addrId }
  }
}
