package com.tnmaster.domain.shared

import java.time.LocalDateTime

/**
 * 领域事件接口，表示领域中发生的事件
 */
interface DomainEvent {
    /**
     * 事件发生的时间
     */
    val occurredOn: LocalDateTime
} 
