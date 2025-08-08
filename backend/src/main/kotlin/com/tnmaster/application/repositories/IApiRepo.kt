package com.tnmaster.application.repositories

import com.tnmaster.dto.api.ApiPermitVariantView
import com.tnmaster.entities.*
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.enums.HttpMethod
import io.github.truenine.composeserver.rds.IRepo
import io.github.truenine.composeserver.rds.fetchPq
import io.github.truenine.composeserver.rds.toFetcher
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.babyfish.jimmer.sql.kt.ast.table.isNotNull
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IApiRepo : IRepo<Api, RefId> {
  fun findAllBySpec(spec: KSpecification<Api>, pq: Pq? = Pq.DEFAULT_MAX): IPage<Api> {
    return sql
      .createQuery(Api::class) {
        where(spec)
        orderBy(table.permissionsId, table.requireLogin, table.apiPath, table.apiMethod, table.name)
        select(table.fetch(allFetcher))
      }
      .fetchPq(pq)
  }

  /** ## 获取所有 带有权限的 API */
  fun findALlPermissionsVariant(): List<ApiPermitVariantView> {
    return sql
      .createQuery(Api::class) {
        where(and(table.apiPath.isNotNull(), table.apiMethod.isNotNull(), or(table.permissions.isNotNull(), table.requireLogin `eq?` true)))
        select(table.fetch(variantFetcher))
      }
      .execute()
      .map { ApiPermitVariantView(it) }
  }

  fun findAllByApiPathInAndApiMethodIn(apiPaths: Set<String>, apiMethods: Set<HttpMethod>, fetcher: Fetcher<Api> = allFetcher): List<Api> {
    return sql
      .createQuery(Api::class) {
        where += table.apiPath valueIn apiPaths
        where += table.apiMethod valueIn apiMethods
        select(table.fetch(fetcher))
      }
      .execute()
  }

  companion object {
    @JvmStatic
    val allFetcher =
      newFetcher(Api::class).by {
        allScalarFields()
        permissions { allScalarFields() }
      }

    @JvmStatic
    val variantFetcher = ApiPermitVariantView::class.toFetcher()
  }
}
