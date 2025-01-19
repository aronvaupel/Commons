package com.ecommercedemo.common.controller.abstraction.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to create a new entity.")
data class CreateRequest(
    val entityClassName: String,
    val properties: Map<String, Any?> = emptyMap()
)