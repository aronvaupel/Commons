package com.ecommercedemo.common.controller.abstraction.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to create a new entity.")
data class CreateRequest @JsonCreator constructor(
    @JsonProperty("entityClassName") val entityClassName: String,
    @JsonProperty("properties") val properties: Map<String, Any?> = emptyMap()
)