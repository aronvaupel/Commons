package com.ecommercedemo.common.util.springboot

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(name = ["jakarta.persistence.EntityManagerFactory"])
class EntityScanner @Autowired constructor(
    private val entityManagerFactory: EntityManagerFactory,
) {
    private fun getEntityNames(filterCondition: (String) -> Boolean): List<String> {
        val entityManager = entityManagerFactory.createEntityManager()
        val metamodel = entityManager.metamodel
        return metamodel.entities
            .map { it.name }
            .filter(filterCondition)
    }

    fun getUpstreamEntityNames(): List<String> {
        println("Scanning for upstream entities")
        return getEntityNames { !it.startsWith("_", ignoreCase = true) }
    }

    fun getDownstreamEntityNames(): List<String> {
        println("Scanning for downstream entities")
        return getEntityNames { it.startsWith("_", ignoreCase = true) }
    }
}