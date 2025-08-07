package com.tnmaster.repositories

import com.tnmaster.dto.disinfo.DisInfoAdminSpec
import com.tnmaster.entities.DisInfo
import com.tnmaster.entities.by
import com.tnmaster.entities.certCode
import com.tnmaster.entities.id
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IDisInfoRepo : IRepo<DisInfo, RefId> {
  fun existsByCertCode(certCode: String? = null): Boolean {
    return sql.createQuery(DisInfo::class) {
      where(
        table.certCode eq certCode
      )
      select(table.id)
    }.exists()
  }

  fun findFirstBy(spec: DisInfoAdminSpec, fetcher: Fetcher<DisInfo>? = newFetcher(DisInfo::class).by { allScalarFields() }): DisInfo? {
    return sql
      .createQuery(DisInfo::class) {
        where(spec)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }
}
