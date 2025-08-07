package com.tnmaster.entities.resolvers

import com.tnmaster.entities.Address
import com.tnmaster.entities.code
import com.tnmaster.entities.id
import com.tnmaster.entities.rpi
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
class AddressParentPathResolver(private val sql: KSqlClient) : KTransientResolver<RefId, List<RefId>> {
  class Join : KWeakJoin<Address, Address>() {
    override fun on(source: KNonNullTable<Address>, target: KNonNullTable<Address>): KNonNullExpression<Boolean>? = and(source.id eq target.rpi)
  }

  override fun resolve(ids: Collection<RefId>): Map<RefId, List<RefId>> {
    if (ids.isEmpty()) return emptyMap()
    return sql
      .createQuery(Address::class) {
        where(table.id valueIn ids)
        orderBy(table.code)
        select(table.id, table.asTableEx().weakJoin(Join::class).id)
      }
      .execute()
      .groupBy({ it._1 }, { it._2 })
      .mapValues { it.value.reversed() }
  }
}
