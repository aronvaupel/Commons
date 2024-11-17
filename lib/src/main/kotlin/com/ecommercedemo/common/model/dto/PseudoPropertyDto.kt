package com.ecommercedemo.common.model.dto

import com.ecommercedemo.common.application.search.TypeDescriptor

data class PseudoPropertyDto(
    val entityClassName: String,
    var key: String,
    var typeDescriptor: TypeDescriptor
)