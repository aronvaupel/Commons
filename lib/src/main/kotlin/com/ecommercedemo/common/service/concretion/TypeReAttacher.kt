package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class TypeReAttacher<T : BaseEntity>(
    val objectMapper: ObjectMapper,
) {
    fun reAttachType(data: Map<String, Any?>, targetClass: KClass<T>): Map<String, Any?> {
        println("DATA: $data")

        val constructor = targetClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No primary constructor for ${targetClass.simpleName}")

        val constructorParameters = constructor.parameters
        println("CONSTRUCTOR PARAMETERS IN TYPE-RE-ATTACHER: $constructorParameters")

        val validatedData = constructor.parameters.associate { param ->
            println("PARAM: $param")
            val value = data[param.name?.removePrefix("_")]
            println("VALUE: $value")
            if (value == null && !param.isOptional && !param.type.isMarkedNullable) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
            println("PARAM.NAME: ${param.name} to VALUE: $value")
            param.name to value
        }.filter { (key, value) ->
            val isExplicitlyPresent = data.containsKey(key)
            value != null || isExplicitlyPresent
        }
        println("VALIDATED DATA: $validatedData")

        val normalizedData = validatedData.mapKeys { (key, _) ->
            println("KEY: $key")
            key?.removePrefix("_")
        }
        println("NORMALIZED DATA: $normalizedData")
        val serializedData = objectMapper.writeValueAsString(normalizedData)

        val dataAsTargetInstance = objectMapper.readValue(
            serializedData,
            targetClass.java
        ) as T

        val typedData = dataAsTargetInstance::class.memberProperties
            .onEach { it.isAccessible = true }
            .associate {
                it.name to it.getter.call(dataAsTargetInstance)
            }
        println("TYPED DATA: $typedData")
        return typedData
    }
}
