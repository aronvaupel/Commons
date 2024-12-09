package com.ecommercedemo.common.application.validation.type

import com.ecommercedemo.common.controller.abstraction.util.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*

@Suppress("unused")
enum class ValueType(
    private val category: TypeCategory,
    val typeInfo: Class<*>
) {

    // === PRIMITIVE TYPES ===
    BYTE(TypeCategory.PRIMITIVE, Byte::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toByte() })
        }
    },
    UBYTE(TypeCategory.PRIMITIVE, UByte::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toUByte() }, { it.all { char -> char.isDigit() } })
        }
    },
    SHORT(TypeCategory.PRIMITIVE, Short::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toShort() })
        }
    },
    USHORT(TypeCategory.PRIMITIVE, UShort::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toUShort() }, { it.all { char -> char.isDigit() } })
        }
    },
    INTEGER(TypeCategory.PRIMITIVE, Int::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toInt() })
        }
    },
    UINT(TypeCategory.PRIMITIVE, UInt::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toUInt() }, { it.all { char -> char.isDigit() } })
        }
    },
    LONG(TypeCategory.PRIMITIVE, Long::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toLong() })
        }
    },
    ULONG(TypeCategory.PRIMITIVE, ULong::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toULong() }, { it.all { char -> char.isDigit() } })
        }
    },
    FLOAT(TypeCategory.PRIMITIVE, Float::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toFloat() })
        }
    },
    DOUBLE(TypeCategory.PRIMITIVE, Double::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { it.toDouble() })
        }
    },

    CHAR(TypeCategory.PRIMITIVE, Char::class.java),

    BOOLEAN(TypeCategory.PRIMITIVE, Boolean::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateBool(value)
        }
    },

    STRING(TypeCategory.PRIMITIVE, String::class.java),

    BIG_INTEGER(TypeCategory.PRIMITIVE, BigInteger::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { BigInteger(it) }, { it.all { char -> char.isDigit() } })
        }
    },
    BIG_DECIMAL(TypeCategory.PRIMITIVE, BigDecimal::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateNumber(value, { BigDecimal(it) })
        }
    },

    // === TIME TYPES ===
    INSTANT(TypeCategory.TIME, java.time.Instant::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    LOCAL_DATE(TypeCategory.TIME, java.time.LocalDate::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    LOCAL_DATE_TIME(TypeCategory.TIME, java.time.LocalDateTime::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    ZONED_DATE_TIME(TypeCategory.TIME, java.time.ZonedDateTime::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    OFFSET_DATE_TIME(TypeCategory.TIME, java.time.OffsetDateTime::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    OFFSET_TIME(TypeCategory.TIME, java.time.OffsetTime::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    DURATION(TypeCategory.TIME, java.time.Duration::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },
    PERIOD(TypeCategory.TIME, java.time.Period::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateTime(value, descriptor as TypeDescriptor.TimeDescriptor)
        }
    },


    // === COLLECTION TYPES ===
    LIST(TypeCategory.COLLECTION, List::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != LIST) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for LIST type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ARRAY_LIST(TypeCategory.COLLECTION, ArrayList::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != ARRAY_LIST) {
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
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != LINKED_LIST) {
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
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != VECTOR) {
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
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != SET) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for SET type")
            }
            return validateCollection(descriptor, value)
        }
    },
    COLLECTION(TypeCategory.COLLECTION, Collection::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != COLLECTION) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for COLLECTION type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ITERABLE(TypeCategory.COLLECTION, Iterable::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != ITERABLE) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for ITERABLE type")
            }
            return validateCollection(descriptor, value)
        }
    },
    ARRAY(TypeCategory.COLLECTION, Array<Any>::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != ARRAY) {
                throw IllegalArgumentException("Descriptor must be a CollectionDescriptor for ARRAY type")
            }
            return validateCollection(descriptor, value)
        }
    },
    QUEUE(TypeCategory.COLLECTION, java.util.Queue::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != QUEUE) {
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
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != DEQUE) {
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
            if (descriptor !is TypeDescriptor.CollectionDescriptor || descriptor.type != STACK) {
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
            if (descriptor !is TypeDescriptor.MapDescriptor || descriptor.type != MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for MAP type")
            }
            return validateMap(value, descriptor, Map::class.java)
        }
    },

    HASH_MAP(TypeCategory.COLLECTION, java.util.HashMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.MapDescriptor || descriptor.type != HASH_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is HashMap<*, *>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.HashMap::class.java)
        }
    },
    TREE_MAP(TypeCategory.COLLECTION, java.util.TreeMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.MapDescriptor || descriptor.type != TREE_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is TreeMap<*, *>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.TreeMap::class.java)
        }
    },
    LINKED_HASH_MAP(TypeCategory.COLLECTION, java.util.LinkedHashMap::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            if (descriptor !is TypeDescriptor.MapDescriptor || descriptor.type != LINKED_HASH_MAP) {
                throw IllegalArgumentException("Descriptor must be a MapDescriptor for map validation")
            }
            if (value !is LinkedHashMap<*, *>) {
                throw IllegalArgumentException("Value must be an ArrayList.")
            }

            return validateMap(value, descriptor, java.util.LinkedHashMap::class.java)
        }
    },

    // === COMPLEX TYPES ===
    OBJECT(TypeCategory.COMPLEX, Any::class.java) {
        override fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
            return validateComplexObject(value, descriptor as TypeDescriptor.ComplexObjectDescriptor)
        }
    },

    // === GENERIC TYPES ===
    ANY(TypeCategory.GENERIC, Any::class.java),
    VOID(TypeCategory.GENERIC, Void::class.java);

    open fun validate(value: Any?, descriptor: TypeDescriptor): Boolean {
        if (descriptor.type != this) {
            throw IllegalArgumentException("Descriptor type mismatch. Expected: $this, Found: ${descriptor.type}")
        }

        isNullable(value, descriptor)

        return when (this.category) {
            TypeCategory.PRIMITIVE -> typeInfo.isInstance(value)
            TypeCategory.TIME -> {
               if (descriptor !is TypeDescriptor.TimeDescriptor) return false
                validateTime(value, descriptor)
            }
            TypeCategory.COLLECTION -> {
                if (descriptor !is TypeDescriptor.CollectionDescriptor) return false
                validateCollection(descriptor, value)
            }

            TypeCategory.MAP -> {
                if (descriptor !is TypeDescriptor.MapDescriptor) return false
                validateMap(value, descriptor, typeInfo)
            }

            TypeCategory.COMPLEX -> {
                if (descriptor !is TypeDescriptor.ComplexObjectDescriptor) return false
                validateComplexObject(value, descriptor)
            }

            else -> false
        }
    }

    companion object {
        fun validateValueAgainstDescriptor(
            descriptor: TypeDescriptor,
            value: Any?,
            failureDetails: MutableList<String> = mutableListOf()
        ): Boolean {
            return when (descriptor) {
                is TypeDescriptor.PrimitiveDescriptor -> {
                    val isValid = descriptor.type.validate(value, descriptor)
                    if (!isValid) {
                        failureDetails.add("Primitive validation failed for value '$value' with descriptor '$descriptor'")
                    }
                    isValid
                }

                is TypeDescriptor.TimeDescriptor -> {
                    val isValid = descriptor.type.validate(value, descriptor)
                    if (!isValid) {
                        failureDetails.add("Time validation failed for value '$value' with descriptor '$descriptor'")
                    }
                    isValid
                }

                is TypeDescriptor.CollectionDescriptor -> {
                    if (value !is Collection<*>) {
                        failureDetails.add("Expected Collection but got ${value?.javaClass?.name} for descriptor '$descriptor'")
                        return false
                    }
                    val itemFailures = value.mapIndexedNotNull { index, item ->
                        if (!validateValueAgainstDescriptor(descriptor.itemDescriptor, item, failureDetails)) {
                            "Item at index $index failed validation."
                        } else null
                    }
                    failureDetails.addAll(itemFailures)
                    itemFailures.isEmpty()
                }

                is TypeDescriptor.MapDescriptor -> {
                    if (value !is Map<*, *>) {
                        failureDetails.add("Expected Map but got ${value?.javaClass?.name} for descriptor '$descriptor'")
                        return false
                    }
                    val keyFailures = value.keys.mapNotNull { key ->
                        if (!validateValueAgainstDescriptor(descriptor.keyDescriptor, key, failureDetails)) {
                            "Key '$key' failed validation."
                        } else null
                    }
                    val valueFailures = value.values.mapNotNull { mapValue ->
                        if (!validateValueAgainstDescriptor(descriptor.valueDescriptor, mapValue, failureDetails)) {
                            "Value '$mapValue' failed validation."
                        } else null
                    }
                    failureDetails.addAll(keyFailures + valueFailures)
                    keyFailures.isEmpty() && valueFailures.isEmpty()
                }

                is TypeDescriptor.ComplexObjectDescriptor -> {
                    if (value !is Map<*, *>) {
                        failureDetails.add("Expected ComplexObject but got ${value?.javaClass?.name} for descriptor '$descriptor'")
                        return false
                    }
                    val fieldFailures = descriptor.fields.mapNotNull { (fieldName, fieldDescriptor) ->
                        val fieldValue = value[fieldName]
                        if (!validateValueAgainstDescriptor(fieldDescriptor, fieldValue, failureDetails)) {
                            "Field '$fieldName' failed validation."
                        } else null
                    }
                    failureDetails.addAll(fieldFailures)
                    fieldFailures.isEmpty()
                }
            }
        }
    }

    fun validateMap(value: Any?, descriptor: TypeDescriptor.MapDescriptor, expectedMapType: Class<*>): Boolean {
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

    fun validateCollection(descriptor: TypeDescriptor.CollectionDescriptor, value: Any?): Boolean {
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

    fun validateComplexObject(value: Any?, descriptor: TypeDescriptor.ComplexObjectDescriptor): Boolean {

        if (value !is Map<*, *>) {
            throw IllegalArgumentException("Complex object value must be a Map.")
        }

        return descriptor.fields.all { (fieldName, fieldDescriptor) ->
            val fieldValue = value[fieldName]

            isNullable(fieldValue, fieldDescriptor)

            ValueType.validateValueAgainstDescriptor(fieldDescriptor, fieldValue)
        }
    }

    fun validateNumber(
        value: Any?,
        parseFunction: (String) -> Any,
        unsignedCheck: ((String) -> Boolean)? = null
    ): Boolean {
        return when (value) {
            is Number -> true
            is String -> try {
                parseFunction(value)
                unsignedCheck?.invoke(value) ?: true
            } catch (e: NumberFormatException) {
                false
            }

            else -> false
        }
    }

    fun validateTime(value: Any?, descriptor: TypeDescriptor.TimeDescriptor): Boolean {

        if (value !is String) {
            return false
        }

        return try {
            when (descriptor.type) {
                INSTANT -> Instant.parse(value)
                LOCAL_DATE -> LocalDate.parse(value)
                LOCAL_DATE_TIME -> LocalDateTime.parse(value)
                ZONED_DATE_TIME -> ZonedDateTime.parse(value)
                OFFSET_DATE_TIME -> OffsetDateTime.parse(value)
                OFFSET_TIME -> OffsetTime.parse(value)
                DURATION -> Duration.parse(value)
                PERIOD -> Period.parse(value)
                else -> return false
            }
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun validateBool(value: Any?): Boolean {
        return when (value) {
            is Boolean -> true
            is String -> value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)
            else -> false
        }
    }

    private fun supportsNullable(descriptor: TypeDescriptor): Boolean {
        return when (descriptor) {
            is TypeDescriptor.PrimitiveDescriptor,
            is TypeDescriptor.TimeDescriptor,
            is TypeDescriptor.ComplexObjectDescriptor -> true

            else -> false
        }
    }

    private fun isNullable(value: Any?, descriptor: TypeDescriptor): Boolean {
        return if (value == null) {
            when (descriptor) {
                is TypeDescriptor.PrimitiveDescriptor -> descriptor.isNullable
                is TypeDescriptor.TimeDescriptor -> descriptor.isNullable
                is TypeDescriptor.ComplexObjectDescriptor -> descriptor.isNullable
                else -> false
            }
        } else false
    }
}


