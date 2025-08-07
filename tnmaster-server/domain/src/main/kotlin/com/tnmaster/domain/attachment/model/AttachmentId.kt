package com.tnmaster.domain.attachment.model

import com.tnmaster.domain.shared.ValueObject
import java.util.*

/**
 * 附件ID值对象
 */
data class AttachmentId(
  val value: String
) : ValueObject {
  companion object {
    /**
     * 生成新的附件ID
     */
    fun generate(): AttachmentId = AttachmentId(UUID.randomUUID().toString())
  }
} 
