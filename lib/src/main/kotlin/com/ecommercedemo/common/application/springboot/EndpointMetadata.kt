package com.ecommercedemo.common.application.springboot

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.reflect.KClass

data class EndpointMetadata(
    val path: String,
    val method: String,
    val roles: Set<String>,
    @JsonIgnore val pathVariables: Map<String, KClass<*>> = emptyMap(),
    @JsonIgnore val requestParams: Map<String, KClass<*>> = emptyMap()
) {
    @JsonProperty("pathVariables")
    fun getPathVariableTypes(): Map<String, String> {
        return pathVariables.mapValues { it.value.simpleName ?: "Unknown" }
    }

    @JsonProperty("requestParams")
    fun getRequestParamTypes(): Map<String, String> {
        return requestParams.mapValues { it.value.simpleName ?: "Unknown" }
    }
}

