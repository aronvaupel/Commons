package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.validation.modification.ModificationType
import java.util.*

data class EntityEvent(
    val entityClassName: String,
    val id: UUID,
    val type: ModificationType,
    val properties: Map<String, Any?>,
)
