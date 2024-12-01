package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.TypeCategory
import com.ecommercedemo.common.application.validation.type.ValueType

data class ComplexObjectDescriptor(
    override val category: TypeCategory,
    override val type: ValueType,
    override val isNullable: Boolean,
    val fields: Map<String, TypeDescriptor>
) : NullableTypeDescriptor
