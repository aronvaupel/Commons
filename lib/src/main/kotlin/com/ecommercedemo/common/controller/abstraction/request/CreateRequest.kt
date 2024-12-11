package com.ecommercedemo.common.controller.abstraction.request

import com.fasterxml.jackson.annotation.JsonTypeInfo

data class CreateRequest(
    val entityClassName: String,
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    val properties: Map<String, Any?> = emptyMap()
)