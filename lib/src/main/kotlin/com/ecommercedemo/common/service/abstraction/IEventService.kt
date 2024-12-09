package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IEventService<T: BaseEntity> {
    fun createByEvent(event: EntityEvent)
    fun updateByEvent(event: EntityEvent)
    fun deleteByEvent(event: EntityEvent)
}