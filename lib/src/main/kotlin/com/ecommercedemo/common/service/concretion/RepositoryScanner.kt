package com.ecommercedemo.common.service.concretion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class RepositoryScanner @Autowired constructor(
    private val applicationContext: ApplicationContext,
) {
    private fun getEntityNames(filterCondition: (String) -> Boolean): List<String> {
        return applicationContext.getBeanNamesForType(JpaRepository::class.java)
            .map { it.dropLast(10) }
            .filter(filterCondition)
    }

    fun getUpstreamEntityNames(): List<String> {
        return getEntityNames { !it.startsWith("_", ignoreCase = true) }
    }

    fun getDownstreamEntityNames(): List<String> {
        return getEntityNames { it.startsWith("_", ignoreCase = true) }
    }
}