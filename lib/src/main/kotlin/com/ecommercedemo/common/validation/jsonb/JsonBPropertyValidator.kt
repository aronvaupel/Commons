package com.ecommercedemo.common.validation.jsonb

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.filter.FilterCriteria
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class JsonBPropertyValidator {

    fun <T : BaseEntity> validateJsonbAttribute(filter: FilterCriteria, entity: T) {
        if (filter.jpaAttribute != "pseudoProperties" || filter.pseudoPropertyPathToKey == null) {
            throw InvalidAttributeException(filter.pseudoPropertyPathToKey ?: "null", filter.entitySimpleName)
        }

        val actualValue = entity.pseudoProperties.getValueByPath(filter.pseudoPropertyPathToKey)
            ?: throw InvalidAttributeException(filter.pseudoPropertyPathToKey, filter.entitySimpleName)

        validateValueType(filter.pseudoPropertyPathToKey, actualValue, filter.value)
    }

    private fun validateValueType(key: String, actualValue: Any?, expectedValue: Any?) {
        if (expectedValue != null && actualValue != null && !actualValue::class.isInstance(expectedValue)) {
            throw ValueTypeMismatchException(
                key, actualValue::class.simpleName ?: "UnknownType", expectedValue::class.simpleName ?: "UnknownType"
            )
        }
    }


    private fun Map<String, Any>.getValueByPath(path: String): Any? {
        return path.split(".").fold(this as Any?) { current, key ->
            when (current) {
                is Map<*, *> -> current[key]

                is List<*> -> current.firstNotNullOfOrNull {
                    it?.getPropertyByName(key)
                }

                else -> current?.getPropertyByName(key)
            }
        }
    }

    private fun Any.getPropertyByName(propertyName: String): Any? {
        return when (this) {
            is Map<*, *> -> this[propertyName]
            else -> this::class.memberProperties
                .firstOrNull { it.name == propertyName }
                ?.apply { isAccessible = true }
                ?.getter
                ?.call(this)
        }
    }
}