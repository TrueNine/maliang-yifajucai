package com.tnmaster.domain.attachment.event

import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentMetadata
import com.tnmaster.domain.attachment.model.AttachmentType
import com.tnmaster.domain.shared.DomainEvent
import java.time.LocalDateTime

/**
 * 附件事件基类
 */
sealed class AttachmentEvent : DomainEvent {
    abstract val id: AttachmentId
    abstract override val occurredOn: LocalDateTime
}

/**
 * 附件创建事件
 */
data class AttachmentCreatedEvent(
    override val id: AttachmentId,
    val metadata: AttachmentMetadata,
    val type: AttachmentType,
    val ownerId: String,
    val path: String,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : AttachmentEvent()

/**
 * 附件删除事件
 */
data class AttachmentDeletedEvent(
    override val id: AttachmentId,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : AttachmentEvent()

/**
 * 附件元数据更新事件
 */
data class AttachmentMetadataUpdatedEvent(
    override val id: AttachmentId,
    val metadata: AttachmentMetadata,
    override val occurredOn: LocalDateTime = LocalDateTime.now()
) : AttachmentEvent() 
