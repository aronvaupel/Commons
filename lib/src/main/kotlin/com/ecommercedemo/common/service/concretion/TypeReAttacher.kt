package com.ecommercedemo.common.service.concretion

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@Service
@Suppress("UNCHECKED_CAST")
class TypeReAttacher(
    val objectMapper: ObjectMapper,
) {
//    fun reAttachType(data: Map<String, Any?>, targetClass: KClass<*>): Map<String, Any?> {

//        val constructor = targetClass.constructors.find { it.parameters.isNotEmpty() }
//            ?: throw IllegalArgumentException("No primary constructor for ${targetClass.simpleName}")

//        val validatedData = constructor.parameters.associate { param ->
//            val value = data[param.name?.removePrefix("_")]
//            if (value == null && !param.isOptional && !param.type.isMarkedNullable) {
//                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
//            }
//            param.name to value
//        }.filter { (key, value) ->
//            val isExplicitlyPresent = data.containsKey(key)
//            value != null || isExplicitlyPresent
//        }

//        val normalizedData = validatedData.mapKeys { (key, _) ->
//            println("KEY: $key")
//            key?.removePrefix("_")
//        }
//        val serializedData = objectMapper.writeValueAsString(normalizedData)

//        val dataAsTargetInstance = objectMapper.readValue(
//            serializedData,
//            targetClass.java
//        )

//        val typedData = dataAsTargetInstance::class.memberProperties
//            .onEach { it.isAccessible = true }
//            .associate {
//                it.name to it.getter.call(dataAsTargetInstance)
//            }
//            .filter { (key, _) ->
//                data.containsKey(key)
//            }
//        println("TYPED DATA: $typedData")
//        return typedData
//    }

    private fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        val result =  entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
        println("FIELD TYPES: $result")
        return result
    }


    fun reAttachType(
        data: Map<String, Any?>,
        entityClass: KClass<*>
    ): Map<String, Any?> {
        println("DATA: $data")
        val  typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }
        println("TYPES FOR DATA KEYS: $typesForDataKeys")

        val typedData: Map<String, Any?> = typesForDataKeys.mapValues { (key, kType) ->
            val typeReference = object : TypeReference<Any>() {
                override fun getType() = kType.javaType
            }
            try {
                objectMapper.convertValue(data[key], typeReference)
            } catch (e: IllegalArgumentException) {
                return@mapValues
            }

        }

        println("TYPED DATA: $typedData")
        return typedData
    }

}
