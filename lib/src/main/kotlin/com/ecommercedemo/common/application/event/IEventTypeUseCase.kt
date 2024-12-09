package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity

interface IEventTypeUseCase<T: BaseEntity> {
    fun applyChanges(event: EntityEvent) {
    }
}