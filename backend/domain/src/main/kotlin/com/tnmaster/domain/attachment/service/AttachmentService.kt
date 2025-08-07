package com.tnmaster.domain.attachment.service

import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentMetadata
import com.tnmaster.domain.attachment.model.AttachmentType
import java.io.InputStream

/**
 * 附件领域服务接口
 */
interface AttachmentService {
  /**
   * 上传附件并返回附件ID
   */
  fun uploadAttachment(
    inputStream: InputStream,
    metadata: AttachmentMetadata,
    type: AttachmentType,
    ownerId: String
  ): AttachmentId

  /**
   * 生成附件访问URL
   */
  fun generateAccessUrl(id: AttachmentId, temporaryAccess: Boolean = false): String

  /**
   * 验证附件类型
   */
  fun validateAttachmentType(mimeType: String, type: AttachmentType): Boolean
} 
