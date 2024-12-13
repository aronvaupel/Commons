package com.ecommercedemo.common.service.concretion

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class TypeReAttacher(
    val objectMapper: ObjectMapper,
) {
    // Fixme: malfunctions on updates: TYPED DATA: {_password=, lastActive=2024-12-14T05:49:17.135869183, password=, userInfo=null, userRole=GUEST, username=, createdAt=2024-12-14T05:49:17.135899975, id=836bf4ce-402d-4234-bac1-7c42b8b580bc, pseudoProperties={}, updatedAt=2024-12-14T05:49:17.135906152}
    fun reAttachType(data: Map<String, Any?>, targetClass: KClass<*>): Map<String, Any?> {

        val constructor = targetClass.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No primary constructor for ${targetClass.simpleName}")

        val validatedData = constructor.parameters.associate { param ->
            val value = data[param.name?.removePrefix("_")]
            if (value == null && !param.isOptional && !param.type.isMarkedNullable) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
            param.name to value
        }.filter { (key, value) ->
            val isExplicitlyPresent = data.containsKey(key)
            value != null || isExplicitlyPresent
        }

        val normalizedData = validatedData.mapKeys { (key, _) ->
            println("KEY: $key")
            key?.removePrefix("_")
        }
        val serializedData = objectMapper.writeValueAsString(normalizedData)

        val dataAsTargetInstance = objectMapper.readValue(
            serializedData,
            targetClass.java
        )

        val typedData = dataAsTargetInstance::class.memberProperties
            .onEach { it.isAccessible = true }
            .associate {
                it.name to it.getter.call(dataAsTargetInstance)
            }
        println("TYPED DATA: $typedData")
        return typedData
    }

//    fun reAttachTypeOnUpdate(data: Any, targetClass: KClass<*>): Any {
//        //same as above, but check if value is present unless in data. if present ignore, if in data override. Account for incoming nulls
//    }
}
