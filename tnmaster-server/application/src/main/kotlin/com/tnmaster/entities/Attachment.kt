package com.tnmaster.entities

import io.github.truenine.composeserver.RefId
import io.github.truenine.composeserver.rds.entities.IEntity
import io.github.truenine.composeserver.rds.typing.AttachmentTyping
import org.babyfish.jimmer.Formula
import org.babyfish.jimmer.sql.*

/** 附件 以及 附件链接 */
@Entity
interface Attachment : IEntity {

  val saveName: String?

  @Key(group = "file")
  val metaName: String?

  val attType: AttachmentTyping

  /** 该链接下的所有子文件 */
  @OneToMany(mappedBy = "parentUrlAttachment")
  val childAttachments: List<Attachment>

  @Key(group = "url")
  val baseUrl: String?

  @Key(group = "url")
  val baseUri: String?
  val urlName: String?
  val urlDoc: String?

  @IdView("parentUrlAttachment")
  val urlId: RefId?

  @Key(group = "file")
  @ManyToOne
  @JoinColumn(name = "url_id")
  @OnDissociate(DissociateAction.SET_NULL)
  val parentUrlAttachment: Attachment?

  @Key(group = "file")
  val size: Long?

  @Key(group = "file")
  val mimeType: String?

  @Formula(dependencies = ["attType", "parentUrlAttachment.baseUrl", "parentUrlAttachment.baseUri", "saveName", "baseUrl", "baseUri"])
  val fullAccessUrl: String?
    get() =
      if (attType == AttachmentTyping.BASE_URL) {
        "$baseUrl/$baseUri"
      } else {
        parentUrlAttachment?.let { a -> a.baseUrl + "/" + a.baseUri + "/" + saveName } ?: saveName
      }
}
