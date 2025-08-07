package com.tnmaster.domain.attachment.command

import com.tnmaster.domain.attachment.AttachmentAggregate
import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.repository.AttachmentRepository
import com.tnmaster.domain.attachment.service.AttachmentService

/**
 * 附件命令处理器
 */
class AttachmentCommandHandler(
  private val repository: AttachmentRepository,
  private val attachmentService: AttachmentService
) {
  /**
   * 处理创建附件命令
   */
  fun handle(command: AttachmentCommand.CreateAttachment): AttachmentId {
    val id = AttachmentId.generate()
    val aggregate = AttachmentAggregate.create(
      id,
      command.metadata,
      command.type,
      command.ownerId,
      command.path
    )
    repository.save(aggregate)
    return id
  }

  /**
   * 处理删除附件命令
   */
  fun handle(command: AttachmentCommand.DeleteAttachment) {
    val attachment = repository.findById(command.id) ?: throw IllegalArgumentException("附件不存在")
    attachment.delete()
    repository.save(attachment)
  }

  /**
   * 处理更新附件元数据命令
   */
  fun handle(command: AttachmentCommand.UpdateAttachmentMetadata) {
    val attachment = repository.findById(command.id) ?: throw IllegalArgumentException("附件不存在")
    attachment.updateMetadata(command.metadata)
    repository.save(attachment)
  }
} 
