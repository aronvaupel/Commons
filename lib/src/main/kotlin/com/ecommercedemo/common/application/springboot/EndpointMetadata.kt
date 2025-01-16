package com.ecommercedemo.common.application.springboot

import kotlin.reflect.KClass

data class EndpointMetadata(
    val path: String,
    val method: String,
    val roles: Set<String>,
    val pathVariables: Map<String, KClass<*>>,
    val requestParams: Map<String, KClass<*>>
)
