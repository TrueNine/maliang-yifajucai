package com.tnmaster.domain.attachment.command

import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentMetadata
import com.tnmaster.domain.attachment.model.AttachmentType

/**
 * 附件命令类
 */
sealed class AttachmentCommand {
    /**
     * 创建附件命令
     */
    data class CreateAttachment(
        val metadata: AttachmentMetadata,
        val type: AttachmentType,
        val ownerId: String,
        val path: String
    ) : AttachmentCommand()
    
    /**
     * 删除附件命令
     */
    data class DeleteAttachment(
        val id: AttachmentId
    ) : AttachmentCommand()
    
    /**
     * 更新附件元数据命令
     */
    data class UpdateAttachmentMetadata(
        val id: AttachmentId,
        val metadata: AttachmentMetadata
    ) : AttachmentCommand()
} 
