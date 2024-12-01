package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.ValueType

data class CollectionDescriptor(
    override val type: ValueType,
    val itemDescriptor: TypeDescriptor,
    val minElements: Int,
    val maxElements: Int?
) : TypeDescriptor