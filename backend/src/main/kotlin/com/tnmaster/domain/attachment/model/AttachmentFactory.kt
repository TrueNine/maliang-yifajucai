package com.tnmaster.domain.attachment.model

import java.time.LocalDateTime

/**
 * 附件工厂类，负责创建附件实体
 */
class AttachmentFactory {
    
    /**
     * 从聚合根创建附件实体
     */
    companion object {
        fun createFromAggregate(
            aggregate: com.tnmaster.domain.attachment.AttachmentAggregate,
            fullAccessUrl: String? = null
        ): Attachment {
            return Attachment(
                id = aggregate.id,
                fileName = aggregate.metadata.fileName,
                mimeType = aggregate.metadata.mimeType,
                size = aggregate.metadata.size,
                path = aggregate.path,
                fullAccessUrl = fullAccessUrl,
                uploadTime = aggregate.metadata.uploadTime,
                metaName = aggregate.metadata.metaName,
                ownerId = aggregate.ownerId,
                type = aggregate.type
            )
        }
    }
} 
