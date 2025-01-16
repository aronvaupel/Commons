package com.ecommercedemo.common.application.springboot

data class EndpointMetadata (
    val path: String,
    val method: String,
    val roles: Set<String>,
    val pathVariables: List<PathVariableRepresentation>,
    val requestParameters: List<RequestParamRepresentation>
)
