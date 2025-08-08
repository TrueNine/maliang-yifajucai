package com.tnmaster.domain.attachment

import com.tnmaster.domain.attachment.event.AttachmentCreatedEvent
import com.tnmaster.domain.attachment.event.AttachmentDeletedEvent
import com.tnmaster.domain.attachment.event.AttachmentMetadataUpdatedEvent
import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentMetadata
import com.tnmaster.domain.attachment.model.AttachmentType
import com.tnmaster.domain.shared.AggregateRoot

/**
 * 附件聚合根
 */
class AttachmentAggregate private constructor(
  val id: AttachmentId,
  var metadata: AttachmentMetadata,
  val type: AttachmentType,
  val ownerId: String,
  val path: String,
  var isDeleted: Boolean = false
) : AggregateRoot<AttachmentId> {

  private val events = ArrayList<Any>()

  companion object {
    /**
     * 创建新的附件聚合根
     */
    fun create(id: AttachmentId, metadata: AttachmentMetadata, type: AttachmentType, ownerId: String, path: String): AttachmentAggregate {
      val attachment = AttachmentAggregate(id, metadata, type, ownerId, path)
      attachment.registerEvent(AttachmentCreatedEvent(id, metadata, type, ownerId, path))
      return attachment
    }
  }

  /**
   * 删除附件
   */
  fun delete() {
    if (!isDeleted) {
      isDeleted = true
      registerEvent(AttachmentDeletedEvent(id))
    }
  }

  /**
   * 更新附件元数据
   */
  fun updateMetadata(metadata: AttachmentMetadata) {
    if (!isDeleted) {
      this.metadata = metadata
      registerEvent(AttachmentMetadataUpdatedEvent(id, metadata))
    }
  }

  /**
   * 获取聚合根ID
   */
  override fun id(): AttachmentId = id

  /**
   * 注册领域事件
   */
  override fun registerEvent(event: com.tnmaster.domain.shared.DomainEvent) {
    events.add(event)
  }

  /**
   * 获取未处理的事件
   */
  fun getUncommittedEvents(): List<Any> {
    return events.toList()
  }

  /**
   * 清除未处理的事件
   */
  fun clearEvents() {
    events.clear()
  }
} 
