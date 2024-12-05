package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IEventService<T: BaseEntity, R: BaseEntity> {
    fun createByEvent(event: EntityEvent<T>)
    fun updateByEvent(event: EntityEvent<T>) : R
    fun deleteByEvent(event: EntityEvent<T>)
}