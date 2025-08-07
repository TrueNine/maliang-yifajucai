package com.tnmaster.repositories

import com.tnmaster.dto.bankcard.BankCardView
import com.tnmaster.entities.*
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.isNotNull
import org.babyfish.jimmer.sql.kt.exists
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IBankCardRepo : IRepo<BankCard, RefId> {
  fun findFirstByIdAndUserAccountId(userAccountId: RefId, id: RefId, fetcher: Fetcher<BankCard>? = null): BankCard? {
    return sql
      .createQuery(BankCard::class) {
        where(table.userAccountId eq userAccountId, table.id eq id)
        select(table.fetch(fetcher))
      }
      .execute()
      .firstOrNull()
  }

  fun existsByCode(code: String?): Boolean {
    if (code.isNullOrEmpty()) return false
    return sql.exists(BankCard::class) { where(table.code.isNotNull(), table.code eq code) }
  }

  fun findBankCardsByUserAccountId(userAccountId: RefId): List<BankCardView> {
    return sql
      .createQuery(BankCard::class) {
        where(table.userAccountId eq userAccountId)
        orderBy(table.available, table.orderWeight)
        select(table.fetch(BankCardView::class))
      }
      .execute()
  }
}
