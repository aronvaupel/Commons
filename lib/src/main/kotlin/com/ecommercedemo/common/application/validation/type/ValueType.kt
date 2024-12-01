package com.ecommercedemo.common.application.validation.type

import com.ecommercedemo.common.controller.abstraction.util.*

@Suppress("unused")
enum class ValueType(
    val category: TypeCategory,
    val typeInfo: Class<*>
) {

    // === PRIMITIVE TYPES ===
    BYTE(TypeCategory.PRIMITIVE, Byte::class.java),
    UBYTE(TypeCategory.PRIMITIVE, UByte::class.java),
    SHORT(TypeCategory.PRIMITIVE, Short::class.java),
    USHORT(TypeCategory.PRIMITIVE, UShort::class.java),
    INTEGER(TypeCategory.PRIMITIVE, Int::class.java),
    INT(TypeCategory.PRIMITIVE, Int::class.java),
    UINT(TypeCategory.PRIMITIVE, UInt::class.java),
    LONG(TypeCategory.PRIMITIVE, Long::class.java),
    ULONG(TypeCategory.PRIMITIVE, ULong::class.java),
    FLOAT(TypeCategory.PRIMITIVE, Float::class.java),
    DOUBLE(TypeCategory.PRIMITIVE, Double::class.java),
    CHAR(TypeCategory.PRIMITIVE, Char::class.java),
    BOOLEAN(TypeCategory.PRIMITIVE, Boolean::class.java),
    STRING(TypeCategory.PRIMITIVE, String::class.java),

    BIG_INTEGER(TypeCategory.PRIMITIVE, java.math.BigInteger::class.java),
    BIG_DECIMAL(TypeCategory.PRIMITIVE, java.math.BigDecimal::class.java),

    // === TIME TYPES ===
    INSTANT(TypeCategory.TIME, java.time.Instant::class.java),
    LOCAL_DATE(TypeCategory.TIME, java.time.LocalDate::class.java),
    LOCAL_DATE_TIME(TypeCategory.TIME, java.time.LocalDateTime::class.java),
    ZONED_DATE_TIME(TypeCategory.TIME, java.time.ZonedDateTime::class.java),
    OFFSET_DATE_TIME(TypeCategory.TIME, java.time.OffsetDateTime::class.java),
    OFFSET_TIME(TypeCategory.TIME, java.time.OffsetTime::class.java),
    DURATION(TypeCategory.TIME, java.time.Duration::class.java),
    PERIOD(TypeCategory.TIME, java.time.Period::class.java),

    // === COLLECTION TYPES ===
    LIST(TypeCategory.COLLECTION, List::class.java),
    SET(TypeCategory.COLLECTION, Set::class.java),
    MAP(TypeCategory.COLLECTION, Map::class.java),
    COLLECTION(TypeCategory.COLLECTION, Collection::class.java),
    ITERABLE(TypeCategory.COLLECTION, Iterable::class.java),
    ARRAY(TypeCategory.COLLECTION, Array<Any>::class.java),
    QUEUE(TypeCategory.COLLECTION, java.util.Queue::class.java),
    DEQUE(TypeCategory.COLLECTION, java.util.Deque::class.java),
    STACK(TypeCategory.COLLECTION, java.util.Stack::class.java),

    // === COMPLEX TYPES ===
    OBJECT(TypeCategory.COMPLEX, Any::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is ComplexObjectDescriptor) {
                throw IllegalArgumentException("Descriptor must be a ComplexObjectDescriptor for OBJECT type")
            }

            // Validate each field in the complex object
            if (value !is Map<*, *>) return false
            return descriptor.fields.all { (fieldName, fieldDescriptor) ->
                val fieldValue = value[fieldName]
                validateValueAgainstDescriptor(fieldDescriptor, fieldValue)
            }
        }
    },

    // === GENERIC TYPES ===
    ANY(TypeCategory.GENERIC, Any::class.java),
    VOID(TypeCategory.GENERIC, Void::class.java);

    open fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
        return when (this.category) {
            TypeCategory.PRIMITIVE -> typeInfo.isInstance(value)
            TypeCategory.TIME -> typeInfo.isInstance(value)
            TypeCategory.COLLECTION -> typeInfo.isInstance(value) // Additional nested checks can be added here
            else -> false
        }
    }

    companion object {
        fun validateValueAgainstDescriptor(descriptor: TypeDescriptor, value: Any?): Boolean {
            return when (descriptor) {
                is PrimitiveDescriptor -> descriptor.type.validate(value, descriptor)
                is TimeDescriptor -> descriptor.type.validate(value, descriptor)
                is CollectionDescriptor -> descriptor.type.validate(value, descriptor)
                is ComplexObjectDescriptor -> OBJECT.validate(value, descriptor)
                else -> {
                    throw IllegalArgumentException("Unsupported descriptor type: ${descriptor::class.simpleName}")
                }
            }
        }
    }
}

