package com.ecommercedemo.common.controller.abstraction.request

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

data class UpdateRequest(
    val id: UUID,
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    val properties: Map<String, Any?> = emptyMap()
)