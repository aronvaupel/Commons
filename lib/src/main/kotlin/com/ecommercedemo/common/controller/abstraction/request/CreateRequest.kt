package com.ecommercedemo.common.controller.abstraction.request

data class CreateRequest(
    val entityClassName: String,
    val properties: Map<String, Any?> = emptyMap()
)