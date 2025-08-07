package com.tnmaster.domain.attachment.model

import java.time.LocalDateTime

/**
 * 附件数据类，用于查询结果
 */
data class Attachment(
    val id: AttachmentId,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val path: String,
    val fullAccessUrl: String?,
    val uploadTime: LocalDateTime,
    val metaName: String?,
    val ownerId: String,
    val type: AttachmentType
) 
