package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.ValueType

data class MapDescriptor(
    override val type: ValueType,
    val keyDescriptor: TypeDescriptor,
    val valueDescriptor: TypeDescriptor,
    val minEntries: Int,
    val maxEntries: Int?
) : TypeDescriptor
