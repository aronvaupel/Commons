package com.ecommercedemo.common.application.validation.type

import com.ecommercedemo.common.controller.abstraction.util.*
import java.util.*

@Suppress("unused")
enum class ValueType(
    private val category: TypeCategory,
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
    LIST(TypeCategory.COLLECTION, List::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != LIST) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for LIST type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ARRAY_LIST(TypeCategory.COLLECTION, ArrayList::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != ARRAY_LIST) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for ARRAY_LIST type")
            }
            if (value !is ArrayList<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },
    LINKED_LIST(TypeCategory.COLLECTION, LinkedList::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != LINKED_LIST) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for LINKED_LIST type")
            }
            if (value !is LinkedList<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },
    VECTOR(TypeCategory.COLLECTION, Vector::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != VECTOR) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for VECTOR type")
            }
            if (value !is Vector<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },
    SET(TypeCategory.COLLECTION, Set::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != SET) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for SET type")
            }
            return validateCollection(descriptor, value)
        }
    },
    COLLECTION(TypeCategory.COLLECTION, Collection::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != COLLECTION) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for COLLECTION type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ITERABLE(TypeCategory.COLLECTION, Iterable::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != ITERABLE) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for ITERABLE type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ARRAY(TypeCategory.COLLECTION, Array<Any>::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != ARRAY) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for ARRAY type")
            }
            return validateCollection(descriptor, value)
        }
    },
    QUEUE(TypeCategory.COLLECTION, java.util.Queue::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != QUEUE) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for QUEUE type")
            }
            if (value !is Queue<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },
    DEQUE(TypeCategory.COLLECTION, java.util.Deque::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != DEQUE) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for DEQUE type")
            }
            if (value !is Deque<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },
    STACK(TypeCategory.COLLECTION, java.util.Stack::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is CollectionDescriptor || descriptor.type != STACK) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for STACK type")
            }
            if (value !is Stack<*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateCollection(descriptor, value)
        }
    },

    // === MAP TYPES ===
    MAP(TypeCategory.COLLECTION, Map::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is MapDescriptor || descriptor.type != MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for MAP type")
            }
            return validateMap(value, descriptor, Map::class.java)
        }
    },

    HASH_MAP(TypeCategory.COLLECTION, java.util.HashMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is MapDescriptor || descriptor.type != HASH_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is HashMap<*,*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.HashMap::class.java)
        }
    },
    TREE_MAP(TypeCategory.COLLECTION, java.util.TreeMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is MapDescriptor || descriptor.type != TREE_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is TreeMap<*,*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.TreeMap::class.java)
        }
    },
    LINKED_HASH_MAP(TypeCategory.COLLECTION, java.util.LinkedHashMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is MapDescriptor || descriptor.type != LINKED_HASH_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is LinkedHashMap<*,*>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.LinkedHashMap::class.java)
        }
    },

    // === COMPLEX TYPES ===
    OBJECT(TypeCategory.COMPLEX, Any::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateComplexObject(value, descriptor as ComplexObjectDescriptor)
        }
    },

    // === GENERIC TYPES ===
    ANY(TypeCategory.GENERIC, Any::class.java),
    VOID(TypeCategory.GENERIC, Void::class.java);

    open fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
        if (descriptor.type != this) {
            throw IllegalArgumentException("Descriptor type mismatch. Expected: $this, Found: ${descriptor.type}")
        }

        if (descriptor is NullableTypeDescriptor && value == null) {
            return descriptor.isNullable
        }
        return when (this.category) {
            TypeCategory.PRIMITIVE, TypeCategory.TIME -> typeInfo.isInstance(value)
            TypeCategory.COLLECTION -> {
                if (descriptor !is CollectionDescriptor) return false
                validateCollection(descriptor, value)
            }
            TypeCategory.MAP -> {
                if (descriptor !is MapDescriptor) return false
                validateMap(value, descriptor, typeInfo)
            }
            TypeCategory.COMPLEX -> {
                if (descriptor !is ComplexObjectDescriptor) return false
                validateComplexObject(value, descriptor)
            }
            else -> false
        }
    }

    companion object {
        fun validateValueAgainstDescriptor(descriptor: TypeDescriptor, value: Any?): Boolean {
            return when (descriptor) {
                is PrimitiveDescriptor -> descriptor.type.validate(value, descriptor)
                is TimeDescriptor -> descriptor.type.validate(value, descriptor)
                is CollectionDescriptor -> descriptor.type.validate(value, descriptor)
                is MapDescriptor -> descriptor.type.validate(value, descriptor)
                is ComplexObjectDescriptor -> descriptor.type.validate(value, descriptor)
                else -> {
                    throw IllegalArgumentException("Unsupported descriptor type: ${descriptor::class.simpleName}")
                }
            }
        }
    }

    fun validateMap(value: Any?, descriptor: MapDescriptor, expectedMapType: Class<*>): Boolean {
        if (value !is Map<*, *>) return false
        if (!expectedMapType.isInstance(value)) {
            throw IllegalArgumentException("Value must be of type ${expectedMapType.simpleName}")
        }

        if (value.size < descriptor.minEntries) {
            throw IllegalArgumentException("Map must have at least ${descriptor.minEntries} key-value pairs")
        }
        if (descriptor.maxEntries != null && value.size > descriptor.maxEntries) {
            throw IllegalArgumentException("Map must have at most ${descriptor.maxEntries} key-value pairs")
        }
        return value.keys.all { key ->
            ValueType.validateValueAgainstDescriptor(descriptor.keyDescriptor, key)
        } && value.values.all { mapValue ->
            ValueType.validateValueAgainstDescriptor(descriptor.valueDescriptor, mapValue)
        }
    }

    fun validateCollection(descriptor: CollectionDescriptor, value: Any?): Boolean {
        if (value !is Collection<*>) return false

        if (value.size < descriptor.minElements) {
            throw IllegalArgumentException("Collection must have at least ${descriptor.minElements} elements.")
        }

        if (descriptor.maxElements != null && value.size > descriptor.maxElements) {
            throw IllegalArgumentException("Collection must have at most ${descriptor.maxElements} elements.")
        }

        return value.all { item ->
            ValueType.validateValueAgainstDescriptor(descriptor.itemDescriptor, item)
        }
    }

    fun validateComplexObject(value: Any?, descriptor: ComplexObjectDescriptor): Boolean {
        if (value == null) return descriptor.isNullable

        if (value !is Map<*, *>) {
            throw IllegalArgumentException("Complex object value must be a Map.")
        }

        return descriptor.fields.all { (fieldName, fieldDescriptor) ->
            val fieldValue = value[fieldName]

            if (fieldValue == null && fieldDescriptor is NullableTypeDescriptor && !fieldDescriptor.isNullable) {
                throw IllegalArgumentException("Field '$fieldName' is required but was not provided.")
            }

            ValueType.validateValueAgainstDescriptor(fieldDescriptor, fieldValue)
        }
    }
}


