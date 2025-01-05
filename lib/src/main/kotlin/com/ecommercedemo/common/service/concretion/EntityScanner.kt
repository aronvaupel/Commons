package com.ecommercedemo.common.service.concretion

import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

//Todo: This should rather check for repositories instead of entities
@Component
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
        return getEntityNames { !it.startsWith("_", ignoreCase = true) }
    }

    fun getDownstreamEntityNames(): List<String> {
        return getEntityNames { it.startsWith("_", ignoreCase = true) }
    }
}