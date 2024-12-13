package com.ecommercedemo.common.application.kafka

import java.util.*


data class EntityEvent(
    val entityClassName: String,
    val id: UUID,
    val type: EntityEventType,
    val properties: Map<String, Any?>,
)
