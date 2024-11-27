package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.model.abstraction.BaseEntity
import java.util.*


data class EntityEvent<T: BaseEntity>(
    val entityClass: Class<T>,
    val id: UUID,
    val type: EntityEventType,
    val properties: MutableMap<String, Any?>,
)
