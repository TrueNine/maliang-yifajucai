package com.tnmaster.domain.attachment.query

import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentType

/**
 * 附件查询类
 */
sealed class AttachmentQuery {
    /**
     * 根据ID获取附件
     */
    data class GetById(
        val id: AttachmentId
    ) : AttachmentQuery()
    
    /**
     * 根据所有者ID获取附件列表
     */
    data class GetByOwnerId(
        val ownerId: String,
        val page: Int = 0,
        val size: Int = 20
    ) : AttachmentQuery()
    
    /**
     * 根据附件类型获取附件列表
     */
    data class GetByType(
        val type: AttachmentType,
        val page: Int = 0,
        val size: Int = 20
    ) : AttachmentQuery()
    
    /**
     * 根据文件名搜索附件
     */
    data class SearchByFileName(
        val fileName: String,
        val page: Int = 0,
        val size: Int = 20
    ) : AttachmentQuery()
} 
