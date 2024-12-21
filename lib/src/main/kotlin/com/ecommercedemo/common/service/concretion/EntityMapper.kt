package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.ClassPathScanner
import jakarta.persistence.Entity
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.declaredMemberProperties

@Component
@DependsOn("classPathScanner")
class EntityMapper(
    private val classPathScanner: ClassPathScanner,
) {
    fun mapEntities(): Map<String, Any> {
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

}
