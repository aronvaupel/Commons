package com.ecommercedemo.common.util.springboot

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.PersistenceUnit
import org.springframework.stereotype.Component

@Component
class EntityScanner(
    @PersistenceUnit private val entityManagerFactory: EntityManagerFactory,
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