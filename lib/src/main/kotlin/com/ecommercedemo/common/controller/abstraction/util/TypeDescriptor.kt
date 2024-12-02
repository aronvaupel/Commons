package com.ecommercedemo.common.controller.abstraction.util


import com.ecommercedemo.common.application.validation.type.TypeCategory
import com.ecommercedemo.common.application.validation.type.ValueType
import com.fasterxml.jackson.annotation.JsonTypeName

sealed class TypeDescriptor {
    abstract val category: TypeCategory
    abstract val type: ValueType

    @JsonTypeName("PRIMITIVE")
    data class PrimitiveDescriptor(
        override val category: TypeCategory = TypeCategory.PRIMITIVE,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    @JsonTypeName("TIME")
    data class TimeDescriptor(
        override val category: TypeCategory = TypeCategory.TIME,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    @JsonTypeName("COLLECTION")
    data class CollectionDescriptor(
        override val category: TypeCategory = TypeCategory.COLLECTION,
        override val type: ValueType,
        val itemDescriptor: TypeDescriptor,
        val minElements: Int,
        val maxElements: Int?
    ) : TypeDescriptor()

    @JsonTypeName("MAP")
    data class MapDescriptor(
        override val category: TypeCategory = TypeCategory.MAP,
        override val type: ValueType,
        val keyDescriptor: TypeDescriptor,
        val valueDescriptor: TypeDescriptor,
        val minEntries: Int,
        val maxEntries: Int?
    ) : TypeDescriptor()

    @JsonTypeName("COMPLEX")
    data class ComplexObjectDescriptor(
        override val category: TypeCategory = TypeCategory.COMPLEX,
        override val type: ValueType,
        val isNullable: Boolean,
        val fields: Map<String, TypeDescriptor>
    ) : TypeDescriptor()
}

