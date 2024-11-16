package com.ecommercedemo.common.model.dto

data class PseudoPropertyDto(
    val entityClassName: String,
    var key: String,
    var valueType: Any
)