package com.ecommercedemo.common.service.concretion

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@Service
class TypeReAttacher(
    val objectMapper: ObjectMapper,
) {
    private fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        val result = entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
        return result
    }


    fun reAttachType(
        data: Map<String, Any?>,
        entityClass: KClass<*>
    ): Map<String, Any?> {
        val typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }

        val typedData: Map<String, Any?> = typesForDataKeys.mapValues { (key, kType) ->
            val typeReference = object : TypeReference<Any>() {
                override fun getType() = kType.javaType
            }
            objectMapper.convertValue(data[key], typeReference)
        }
        return typedData
    }

}
