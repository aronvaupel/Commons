package com.ecommercedemo.common.controller.abstraction.request

import java.util.*

data class UpdateRequest(
    val id: UUID,
    val properties: Map<String, Any?> = emptyMap()
)