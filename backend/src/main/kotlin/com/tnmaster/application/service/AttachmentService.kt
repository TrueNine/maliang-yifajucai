package com.tnmaster.application.service

import com.tnmaster.dto.attachment.AttachmentExistsSpec
import com.tnmaster.entities.Attachment
import com.tnmaster.entities.by
import com.tnmaster.application.repositories.IAttachmentRepo
import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.domain.IReadableAttachment
import io.github.truenine.composeserver.oss.ObjectStorageService
import io.github.truenine.composeserver.rds.annotations.ACID
import io.github.truenine.composeserver.rds.enums.AttachmentTyping
import io.github.truenine.composeserver.slf4j
import io.github.truenine.composeserver.toId
import kotlinx.coroutines.runBlocking
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service
import io.github.truenine.composeserver.enums.MediaTypes as MimeTypes

/** 新附件服务 */
@Service
class AttachmentService(
  val attRepo: IAttachmentRepo,
  private val oss: ObjectStorageService,
) {
  companion object {
    @JvmStatic
    private val log = slf4j<AttachmentService>()
  }

  data class ComputedUploadRecord(
    val metaName: String,
    val baseUrl: String,
    val saveName: String = metaName,
    val baseUri: String = "",
    val size: Long = 0,
    val mimeType: MimeTypes = MimeTypes.BINARY,
  )

  /** ## 带副作用地删除附件 根据 id */
  @ACID
  fun effectDeleteById(id: RefId) {
    log.trace("[effectDeleteById] called with id={}", id)
    return effectDeleteAllById(listOf(id))
  }

  /** ## 带副作用地删除附件 根据 id */
  @ACID
  fun effectDeleteAllById(ids: List<RefId>) {
    log.trace("[effectDeleteAllById] called with ids={}", ids)
    val att = attRepo.findAttachmentViewsByIds(ids).firstOrNull()
    log.trace("[effectDeleteAllById] fetched att={}", att)
    att?.let {
      checkNotNull(it.parentUrlAttachment) { "parentUrlAttachment is null" }
      checkNotNull(it.parentUrlAttachment.baseUri) { "parentUrlAttachment.baseUri is null" }
      checkNotNull(it.saveName)
      log.trace("[effectDeleteAllById] removing object with baseUri={}, saveName={}", it.parentUrlAttachment.baseUri, it.saveName)
      runBlocking { oss.deleteObject(it.parentUrlAttachment.baseUri, it.saveName) }
      attRepo.deleteById(it.id.toId()!!)
      log.trace("[effectDeleteAllById] deleted attachment id={}", it.id)
    }
  }

  private fun foundBy(attachmentSpec: AttachmentExistsSpec, fetcher: Fetcher<Attachment> = newFetcher(Attachment::class).by { allTableFields() }): Attachment? {
    log.trace("[foundBy] called with attachmentSpec={}, fetcher={}", attachmentSpec, fetcher)
    val result = attRepo.sql
      .createQuery(Attachment::class) {
        where(attachmentSpec)
        select(table.fetch(fetcher = fetcher))
      }
      .limit(1)
      .offset(0)
      .fetchOneOrNull()
    log.trace("[foundBy] result={}", result)
    return result
  }

  @ACID
  fun fetchOrPostAttachmentByBaseUrlAndUri(
    url: String,
    uri: String,
    fetcher: Fetcher<Attachment> = newFetcher(Attachment::class).by { allScalarFields() },
  ): Attachment {
    log.trace("[fetchOrPostAttachmentByBaseUrlAndUri] called with url={}, uri={}, fetcher={}", url, uri, fetcher)
    val found = attRepo.findByBaseUrlAndBaseUri(url, uri, fetcher)
    if (found != null) {
      log.trace("[fetchOrPostAttachmentByBaseUrlAndUri] found existing attachment: {}", found)
      return found
    }
    val result = attRepo.saveCommand(
      Attachment {
        baseUrl = url
        baseUri = uri
        attType = AttachmentTyping.BASE_URL
      },
      SaveMode.INSERT_IF_ABSENT,
      AssociatedSaveMode.REPLACE
    ).execute(fetcher).modifiedEntity
    log.trace("[fetchOrPostAttachmentByBaseUrlAndUri] created new attachment: {}", result)
    return result
  }

  @ACID
  fun recordUpload(att: IReadableAttachment, remoteCaller: (readableAttachment: IReadableAttachment) -> ComputedUploadRecord): Attachment {
    log.trace("[recordUpload] called with att={}", att)
    val computed = remoteCaller(att)
    log.trace("[recordUpload] computed upload record: {}", computed)
    val url = fetchOrPostAttachmentByBaseUrlAndUri(computed.baseUrl, computed.baseUri)
    log.trace("[recordUpload] got or created parent url attachment: {}", url)
    val fetcher = newFetcher(Attachment::class).by {
      allScalarFields()
      parentUrlAttachment {
        allScalarFields()
      }
    }
    val result = attRepo.saveCommand(
      Attachment {
        parentUrlAttachment = url
        attType = AttachmentTyping.ATTACHMENT
        saveName = computed.saveName
        metaName = computed.metaName
        mimeType = computed.mimeType.value
        size = computed.size.toLong()
      },
      SaveMode.INSERT_IF_ABSENT,
      AssociatedSaveMode.APPEND_IF_ABSENT
    ).execute(fetcher).modifiedEntity

    log.trace("[recordUpload] inserted attachment: {}", result)
    return result
  }
}
