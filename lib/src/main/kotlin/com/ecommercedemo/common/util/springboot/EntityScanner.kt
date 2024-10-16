package com.ecommercedemo.common.util.springboot

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.PersistenceUnit
import org.springframework.stereotype.Component

@Component
class EntityScanner(
    @PersistenceUnit private val entityManagerFactory: EntityManagerFactory,
) {

    fun getEntityNames(): List<String> {
        val entityManager = entityManagerFactory.createEntityManager()
        val metamodel = entityManager.metamodel
        return metamodel.entities
            .map { it.name }
            .filter { !it.contains("downstream", ignoreCase = true) }
    }
}