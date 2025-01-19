package com.ecommercedemo.common.controller.abstraction.request

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Request to update an entity.")
data class UpdateRequest(
    val id: UUID,
    val properties: Map<String, Any?> = emptyMap()
)


