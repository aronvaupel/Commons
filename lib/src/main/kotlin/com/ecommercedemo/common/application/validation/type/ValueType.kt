package com.ecommercedemo.common.application.validation.type

import com.ecommercedemo.common.controller.abstraction.util.TypeCategory

@Suppress("unused")
enum class ValueType(val category: TypeCategory) {
    // === PRIMITIVE TYPES ===
    BYTE(TypeCategory.PRIMITIVE),
    UBYTE(TypeCategory.PRIMITIVE),
    SHORT(TypeCategory.PRIMITIVE),
    USHORT(TypeCategory.PRIMITIVE),
    INTEGER(TypeCategory.PRIMITIVE),
    INT(TypeCategory.PRIMITIVE),
    UINT(TypeCategory.PRIMITIVE),
    LONG(TypeCategory.PRIMITIVE),
    ULONG(TypeCategory.PRIMITIVE),
    FLOAT(TypeCategory.PRIMITIVE),
    DOUBLE(TypeCategory.PRIMITIVE),
    CHAR(TypeCategory.PRIMITIVE),
    BOOLEAN(TypeCategory.PRIMITIVE),
    STRING(TypeCategory.PRIMITIVE),

    BIG_INTEGER(TypeCategory.PRIMITIVE),
    BIG_DECIMAL(TypeCategory.PRIMITIVE),

    // === TIME TYPES ===
    INSTANT(TypeCategory.TIME),
    LOCAL_DATE(TypeCategory.TIME),
    LOCAL_DATE_TIME(TypeCategory.TIME),
    ZONED_DATE_TIME(TypeCategory.TIME),
    OFFSET_DATE_TIME(TypeCategory.TIME),
    OFFSET_TIME(TypeCategory.TIME),
    DURATION(TypeCategory.TIME),
    PERIOD(TypeCategory.TIME),

    // === COLLECTION TYPES ===
    LIST(TypeCategory.COLLECTION),
    SET(TypeCategory.COLLECTION),
    MAP(TypeCategory.COLLECTION),
    COLLECTION(TypeCategory.COLLECTION),
    ITERABLE(TypeCategory.COLLECTION),
    ARRAY(TypeCategory.COLLECTION),
    QUEUE(TypeCategory.COLLECTION),
    DEQUE(TypeCategory.COLLECTION),
    STACK(TypeCategory.COLLECTION),

    // === COMPLEX TYPES ===
    OBJECT(TypeCategory.COMPLEX),

    // === GENERIC TYPES ===
    ANY(TypeCategory.GENERIC),
    VOID(TypeCategory.GENERIC);

    companion object {
        fun isOfCategory(type: ValueType, category: TypeCategory): Boolean {
            return type.category == category
        }
    }
}