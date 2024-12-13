package com.ecommercedemo.common.application.kafka

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

@JsonDeserialize(using = EntityEventDeserializer::class)
data class EntityEvent(
    val entityClassName: String,
    val id: UUID,
    val type: EntityEventType,
    val properties: Map<String, Any?>,
)
