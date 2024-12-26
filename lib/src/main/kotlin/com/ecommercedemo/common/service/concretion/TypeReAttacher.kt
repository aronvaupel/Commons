package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.SpringContextProvider
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManagerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

@Service

class TypeReAttacher(
    private val entityManagerFactory: EntityManagerFactory
) {
    private fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        println("Extracting field types for entity class: $entityClass")
        return entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
    }

    fun reAttachType(
        data: Map<String, Any?>,
        entityClassName: String,
    ): Map<String, Any?> {
        println("Reattaching type for entity class: $entityClassName")
        val entityClass = resolveEntityClass(entityClassName)
        val typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }
        println("Types for data keys: $typesForDataKeys")

        val typedData: Map<String, Any?> = typesForDataKeys.mapValues { (key, kType) ->
            val typeReference = object : TypeReference<Any>() {
                override fun getType() = kType.javaType
            }
            SpringContextProvider.applicationContext.getBean(ObjectMapper::class.java)
                .convertValue(data[key], typeReference)
        }
        return typedData
    }

    private fun resolveEntityClass(entityClassName: String): KClass<*> {
        println("Resolving entity class for name: $entityClassName")
        val entityManager = entityManagerFactory.createEntityManager()
        try {
            val entityType = entityManager.metamodel.entities.find {
                it.javaType.simpleName == entityClassName
            } ?: throw IllegalArgumentException("Entity class not found for name: $entityClassName")
            val result = entityType.javaType.kotlin
            println("Resolved entity class: $result")
            return result
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Entity class not found for name: $entityClassName", e)
        }
    }

}
