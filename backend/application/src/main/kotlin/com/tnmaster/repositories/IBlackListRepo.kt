package com.tnmaster.repositories

import com.tnmaster.entities.BlackList
import com.tnmaster.entities.auditStatus
import com.tnmaster.entities.createDatetime
import com.tnmaster.entities.eventDoc
import io.github.truenine.composeserver.Pq
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IPage
import io.github.truenine.composeserver.rds.IRepo
import io.github.truenine.composeserver.rds.fetchPq
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IBlackListRepo : IRepo<BlackList, RefId> {

  /**
   * ## 分页批量查询黑名单
   *
   * ### 分页依据
   * - 审核状态为空
   * - 审核状态
   * - 创建时间 desc
   * - 事件类型长度
   */
  fun findAllBySpec(spec: KSpecification<BlackList>? = null, pq: Pq? = Pq.DEFAULT_MAX, fetcher: Fetcher<BlackList>? = null): IPage<BlackList> {
    return sql
      .createQuery(BlackList::class) {
        orderBy(table.auditStatus.isNull().desc(), table.auditStatus.asc(), table.createDatetime.desc(), table.eventDoc.asc())
        where(spec)
        select(table.fetch(fetcher))
      }
      .fetchPq(pq)
  }
}
