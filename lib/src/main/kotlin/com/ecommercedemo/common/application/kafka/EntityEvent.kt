package com.ecommercedemo.common.application.kafka

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*


data class EntityEvent(
    val entityClassName: String,
    val id: UUID,
    val type: EntityEventType,
    @JsonDeserialize(using = EventPropertiesDeserializer::class)
    val properties: Map<String, Any?>,
)
