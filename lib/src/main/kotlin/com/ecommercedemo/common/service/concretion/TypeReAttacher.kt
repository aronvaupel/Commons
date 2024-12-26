package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.SpringContextProvider
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaType

@Service
class TypeReAttacher {

    fun <T: BaseEntity>reAttachType(
        data: Map<String, Any?>,
        entityClass: KClass<T>
    ): Map<String, Any?> {
        val typesForDataKeys = SpringContextProvider.applicationContext.getBean(ReflectionService::class.java)
            .extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }

        val typedData: Map<String, Any?> = typesForDataKeys.mapValues { (key, kType) ->
            val typeReference = object : TypeReference<Any>() {
                override fun getType() = kType.javaType
            }
            SpringContextProvider.applicationContext.getBean(ObjectMapper::class.java)
                .convertValue(data[key], typeReference)
        }
        return typedData
    }

}
