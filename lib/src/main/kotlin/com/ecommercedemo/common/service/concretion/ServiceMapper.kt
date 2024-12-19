package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.ClassPathScanner
import jakarta.persistence.Entity
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.functions

@Component
@DependsOn("classPathScanner")
class ServiceMapper(
    private val classPathScanner: ClassPathScanner,
) {

    fun mapEntitiesAndServiceClasses(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        val entities = mapEntities()
        result["entities"] = entities

        val services = mapServices()
        result["services"] = services

        return result
    }

    private fun mapEntities(): Map<String, Any> {
        val entities = ConcurrentHashMap<String, Any>()

        val entityClasses = classPathScanner.findClassesWithAnnotation(Entity::class)

        entityClasses.forEach { entityClass ->
            val entityName = entityClass.simpleName
            val fields = ConcurrentHashMap<String, ConcurrentHashMap<Map<String, Any>, List<Int>>>()

            entityClass.kotlin.declaredMemberProperties.forEach { property ->
                fields[property.name] = ConcurrentHashMap<Map<String, Any>, List<Int>>()
            }
            entities[entityName] = fields
        }

        return entities
    }

    private fun mapServices(): Map<String, Any> {
        val services = ConcurrentHashMap<String, Any>()

        val serviceClasses = classPathScanner.findClassesWithAnnotation(Service::class)

        serviceClasses.forEach { serviceClass ->
            val serviceName = serviceClass.simpleName
            val methods = ConcurrentHashMap<String, Any>()

            serviceClass.kotlin.functions.forEach { function ->
                if (function.annotations.any { it.annotationClass.simpleName == "CachingEligible" }) {
                    methods[function.name] = ConcurrentHashMap<List<Any>, Any>()
                }
            }

            services[serviceName] = methods
        }

        return services
    }
}
