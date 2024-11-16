package com.ecommercedemo.common.application.event

import java.util.*


data class EntityEvent(
    val id: UUID,
    val type: EntityEventType,
    val properties: MutableMap<String, Any?>,
)
