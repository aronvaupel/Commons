package com.ecommercedemo.common.kafka

import org.hibernate.validator.constraints.UUID

data class EntityEvent(
    val id: UUID,
    val type: EntityEventType,
    val  properties: List<ChangedProperty>? = null
)
