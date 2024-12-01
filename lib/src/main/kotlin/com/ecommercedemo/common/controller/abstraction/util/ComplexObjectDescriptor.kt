package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.ValueType

data class ComplexObjectDescriptor(
    override val type: ValueType,
    val fields: Map<String, TypeDescriptor>
) : TypeDescriptor
