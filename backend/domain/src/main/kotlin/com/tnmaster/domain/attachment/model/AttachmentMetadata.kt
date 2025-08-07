package com.tnmaster.domain.attachment.model

import com.tnmaster.domain.shared.ValueObject
import java.time.LocalDateTime

/**
 * 附件元数据值对象
 */
data class AttachmentMetadata(
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val uploadTime: LocalDateTime = LocalDateTime.now(),
    val metaName: String? = null
) : ValueObject 
