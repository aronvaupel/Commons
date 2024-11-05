package com.ecommercedemo.common.kafka

import java.util.*


data class EntityEvent(
    val id: UUID,
    val type: EntityEventType,
    val  properties: List<ChangedProperty>? = null
)
