package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.SpringContextProvider
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@Service

class TypeReAttacher {
    private fun<T: BaseEntity> extractFieldTypesMap(entityClass: KClass<T>): Map<String, KType> {
        return entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
    }

        fun <T: BaseEntity>reAttachType(
            data: Map<String, Any?>,
            entityClass: KClass<T>
        ): Map<String, Any?> {
            val typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }

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
