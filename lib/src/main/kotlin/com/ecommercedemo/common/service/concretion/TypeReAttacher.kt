package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Service
@Suppress("UNCHECKED_CAST")
class TypeReAttacher(
    val objectMapper: ObjectMapper,
) {
    fun <T: BaseEntity>reAttachType(data: Map<String, Any?>, targetClass: KClass<T>): Map<String, Any?> {
        val dataAsTargetInstance = objectMapper.readValue(
            objectMapper.writeValueAsString(data),
            targetClass::class.java
        ) as T
        return dataAsTargetInstance::class.memberProperties
            .associate {
                it.name to it.getter.call(dataAsTargetInstance)
            }
    }
}