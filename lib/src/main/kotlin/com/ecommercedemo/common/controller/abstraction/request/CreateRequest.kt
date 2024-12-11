package com.ecommercedemo.common.controller.abstraction.request

import com.ecommercedemo.common.application.PropertiesDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class CreateRequest(
    val entityClassName: String,
    @JsonDeserialize(using = PropertiesDeserializer::class)
    val properties: Map<String, Any?> = emptyMap()
)