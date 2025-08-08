package com.tnmaster.repositories

import com.tnmaster.dto.attachment.AttachmentView
import com.tnmaster.entities.Attachment
import com.tnmaster.entities.attType
import com.tnmaster.entities.baseUri
import com.tnmaster.entities.baseUrl
import com.tnmaster.entities.by
import com.tnmaster.entities.id
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.IRepo
import io.github.truenine.composeserver.rds.enums.AttachmentTyping
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
interface IAttachmentRepo : IRepo<Attachment, RefId> {
  fun findAttachmentViewsByIds(ids: List<RefId>): List<AttachmentView> {
    return sql
      .createQuery(Attachment::class) {
        where(table.id valueIn ids, table.attType eq AttachmentTyping.ATTACHMENT)
        select(table.fetch(AttachmentView::class))
      }
      .execute()
  }

  fun findByBaseUrlAndBaseUri(
    baseUrl: String,
    baseUri: String = "",
    fetcher: Fetcher<Attachment> = newFetcher(Attachment::class).by { allScalarFields() },
  ): Attachment? {
    return sql
      .createQuery(Attachment::class) {
        where(table.attType eq AttachmentTyping.BASE_URL, table.baseUrl eq baseUrl, table.baseUri eq baseUri)
        select(table.fetch(fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }

  fun findIdByBaseUrlAndBaseUri(baseUrl: String, baseUri: String): RefId? {
    return sql
      .createQuery(Attachment::class) {
        where(table.attType eq AttachmentTyping.BASE_URL, table.baseUrl eq baseUrl, table.baseUri eq baseUri)
        select(table.id)
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
  }
}
