package com.tnmaster.domain.attachment.repository

import com.tnmaster.domain.attachment.AttachmentAggregate
import com.tnmaster.domain.attachment.model.Attachment
import com.tnmaster.domain.attachment.model.AttachmentId
import com.tnmaster.domain.attachment.model.AttachmentType

/**
 * 附件仓储接口
 */
interface AttachmentRepository {
    /**
     * 根据ID查找附件聚合根
     */
    fun findById(id: AttachmentId): AttachmentAggregate?
    
    /**
     * 保存附件聚合根
     */
    fun save(attachment: AttachmentAggregate)
    
    /**
     * 根据ID查找附件
     */
    fun findAttachmentById(id: AttachmentId): Attachment?
    
    /**
     * 根据所有者ID查找附件列表
     */
    fun findAttachmentsByOwnerId(ownerId: String, page: Int, size: Int): List<Attachment>
    
    /**
     * 根据附件类型查找附件列表
     */
    fun findAttachmentsByType(type: AttachmentType, page: Int, size: Int): List<Attachment>
    
    /**
     * 根据文件名搜索附件
     */
    fun searchAttachmentsByFileName(fileName: String, page: Int, size: Int): List<Attachment>
} 
