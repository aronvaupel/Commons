package com.ecommercedemo.common.application

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
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