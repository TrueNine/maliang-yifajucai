package com.tnmaster.domain.shared

/**
 * 聚合根接口，表示一个实体的集合，作为一个单元进行操作
 */
interface AggregateRoot<ID> {
    /**
     * 返回聚合根的唯一标识符
     */
    fun id(): ID
    
    /**
     * 注册领域事件
     */
    fun registerEvent(event: DomainEvent) {
        // 在实现类中处理事件注册
    }
} 
