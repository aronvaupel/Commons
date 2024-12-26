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
        return entityClass.memberProperties.associate { property ->
            property.name to property.returnType
        }
    }

        fun reAttachType(
            data: Map<String, Any?>,
            entityClassName: String,
        ): Map<String, Any?> {
            val entityClass = resolveEntityClass(entityClassName)
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

    private fun resolveEntityClass(entityClassName: String): KClass<*> {
        val entityManager = entityManagerFactory.createEntityManager()
        try {
            val entityType = entityManager.metamodel.entities.find {
                it.javaType.simpleName == entityClassName
            } ?: throw IllegalArgumentException("Entity class not found for name: $entityClassName")

            return entityType.javaType.kotlin
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Entity class not found for name: $entityClassName", e)
        }
    }

    }
