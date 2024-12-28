package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.SpringContextProvider
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManagerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

@Service
@Suppress("UNCHECKED_CAST")
class TypeReAttacher(
    private val entityManagerFactory: EntityManagerFactory
) {
    private fun extractFieldTypesMap(entityClass: KClass<*>): Map<String, KType> {
        println("Extracting field types for entity class: $entityClass")
        return entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
    }

    fun reAttachType(data: Map<String, Any?>, entityClassName: String): Map<String, Any?> {
        val entityClass = resolveEntityClass(entityClassName)
        val typesForDataKeys = extractFieldTypesMap(entityClass).filterKeys { data.containsKey(it) }

        return typesForDataKeys.mapValues { (key, kType) ->
            val rawValue = data[key]
            if (rawValue == null) {
                null
            } else if (isBaseEntity(kType)) {
                val nestedEntityClass = (kType.classifier as KClass<*>).simpleName!!
                if (rawValue is Map<*, *>) {
                    reAttachType(rawValue as Map<String, Any?>, nestedEntityClass)
                } else {
                    throw IllegalArgumentException("Field $key must be a map for nested type $nestedEntityClass.")
                }
            } else {
                val expectedType = (kType.classifier as KClass<*>).java
                if (rawValue is Map<*, *>) {
                    SpringContextProvider.applicationContext.getBean(ObjectMapper::class.java)
                        .convertValue(rawValue, expectedType)
                } else {
                    rawValue
                }
            }
        }
    }

    private fun isBaseEntity(kType: KType): Boolean {
        val classifier = kType.classifier as? KClass<*>
        return classifier != null && BaseEntity::class.java.isAssignableFrom(classifier.java)
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
