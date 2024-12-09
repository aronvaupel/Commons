package com.ecommercedemo.common.application.event

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*


data class EntityEvent(
    val entityClassName: String,
    val id: UUID,
    val type: EntityEventType,
    //@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    @JsonDeserialize(using = PropertiesDeserializer::class)
    val properties: MutableMap<String, Any?>,
)
