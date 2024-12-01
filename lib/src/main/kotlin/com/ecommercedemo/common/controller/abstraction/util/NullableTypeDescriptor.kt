package com.ecommercedemo.common.controller.abstraction.util

interface NullableTypeDescriptor : TypeDescriptor {
    val isNullable: Boolean
}