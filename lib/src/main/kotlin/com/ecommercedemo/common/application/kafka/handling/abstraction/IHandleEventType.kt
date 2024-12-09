package com.ecommercedemo.common.application.kafka.handling.abstraction

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IHandleEventType<T: BaseEntity> {
    fun applyChanges(event: EntityEvent) {
    }
}