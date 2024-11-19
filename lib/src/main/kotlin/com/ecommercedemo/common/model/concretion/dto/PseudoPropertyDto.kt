package com.ecommercedemo.common.model.concretion.dto

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor

data class PseudoPropertyDto(
    val entityClassName: String,
    var key: String,
    var typeDescriptor: TypeDescriptor
)