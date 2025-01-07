package com.ecommercedemo.common.service.concretion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class RepositoryScanner @Autowired constructor(
    private val applicationContext: ApplicationContext,
) {
    private fun getRepositoryNames(filterCondition: (String) -> Boolean): List<String> {
        return applicationContext.getBeanNamesForType(JpaRepository::class.java)
            .filter(filterCondition)
    }

    fun getUpstreamRepositoryNames(): List<String> {
        return getRepositoryNames { !it.startsWith("_", ignoreCase = true) }
    }

    fun getDownstreamRepositoryNames(): List<String> {
        return getRepositoryNames { it.startsWith("_", ignoreCase = true) }
    }
}