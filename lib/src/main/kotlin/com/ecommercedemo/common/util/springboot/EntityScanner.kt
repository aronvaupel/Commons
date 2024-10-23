package com.ecommercedemo.common.util.springboot

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.PersistenceUnit
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("entityManagerFactory")
class EntityScanner(
    @PersistenceUnit private val entityManagerFactory: EntityManagerFactory,
) {
    init {
        println("EntityScanner bean created")
    }

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