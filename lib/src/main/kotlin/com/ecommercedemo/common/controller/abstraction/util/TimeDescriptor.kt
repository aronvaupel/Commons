package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.ValueType

data class TimeDescriptor(
    override val type: ValueType,
    override val isNullable: Boolean
) : NullableTypeDescriptor
