package com.tnmaster.application.repositories

import com.tnmaster.entities.CommonKvConfigDbCache
import com.tnmaster.entities.k
import com.tnmaster.entities.v
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface ICommonKvConfigDbCacheRepo : IRepo<CommonKvConfigDbCache, RefId> {
  fun findFirstValueByKey(key: String): String? {
    return sql
      .createQuery(CommonKvConfigDbCache::class) {
        where(table.k eq key)
        select(table.v)
      }
      .limit(1)
      .offset(0)
      .execute()
      .firstOrNull()
  }
}
