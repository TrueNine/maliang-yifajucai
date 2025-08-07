package com.tnmaster.entities.resolvers

import com.tnmaster.entities.Address
import com.tnmaster.entities.UserInfo
import com.tnmaster.entities.addressCode
import com.tnmaster.entities.code
import com.tnmaster.entities.id
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
class UserInfoAddressResolver(private val sql: KSqlClient) : KTransientResolver<RefId, RefId?> {
  class UserInfoAddressJoin : KWeakJoin<UserInfo, Address>() {
    override fun on(source: KNonNullTable<UserInfo>, target: KNonNullTable<Address>): KNonNullExpression<Boolean>? = and(source.addressCode eq target.code)
  }

  override fun resolve(ids: Collection<RefId>): Map<RefId, RefId?> {
    if (ids.isEmpty()) return emptyMap()
    return sql
      .createQuery(UserInfo::class) {
        where(table.id valueIn ids)
        select(table.id, table.asTableEx().weakJoin(UserInfoAddressJoin::class).id)
      }
      .execute()
      .associate { (id, addrId) -> id to addrId }
  }
}
