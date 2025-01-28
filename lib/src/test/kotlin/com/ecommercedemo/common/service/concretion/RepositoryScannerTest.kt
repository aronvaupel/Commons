package com.ecommercedemo.common.service.concretion

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import org.springframework.data.jpa.repository.JpaRepository

class RepositoryScannerTest {

    private lateinit var applicationContext: ApplicationContext
    private lateinit var repositoryScanner: RepositoryScanner

    @BeforeEach
    fun setup() {
        applicationContext = mock(ApplicationContext::class.java)
        repositoryScanner = RepositoryScanner(applicationContext)
    }

    @Test
    fun `test getUpstreamEntityNames filters correctly`() {
        val beanNames = listOf("OrderRepository", "CustomerRepository", "_AuditLogRepository", "_BackupRepository")
        `when`(applicationContext.getBeanNamesForType(JpaRepository::class.java)).thenReturn(beanNames.toTypedArray())

        val upstreamEntities = repositoryScanner.getUpstreamEntityNames()

        assertNotNull(upstreamEntities)
        assertEquals(listOf("Order", "Customer"), upstreamEntities)
    }

    @Test
    fun `test getDownstreamEntityNames filters correctly`() {
        val beanNames = listOf("OrderRepository", "CustomerRepository", "_AuditLogRepository", "_BackupRepository")
        `when`(applicationContext.getBeanNamesForType(JpaRepository::class.java)).thenReturn(beanNames.toTypedArray())

        val downstreamEntities = repositoryScanner.getDownstreamEntityNames()

        assertNotNull(downstreamEntities)
        assertEquals(listOf("_AuditLog", "_Backup"), downstreamEntities)
    }

    @Test
    fun `test getUpstreamEntityNames with no matching beans`() {
        `when`(applicationContext.getBeanNamesForType(JpaRepository::class.java)).thenReturn(emptyArray())

        val upstreamEntities = repositoryScanner.getUpstreamEntityNames()

        assertNotNull(upstreamEntities)
        assertTrue(upstreamEntities.isEmpty())
    }

    @Test
    fun `test getDownstreamEntityNames with no matching beans`() {
        `when`(applicationContext.getBeanNamesForType(JpaRepository::class.java)).thenReturn(emptyArray())

        val downstreamEntities = repositoryScanner.getDownstreamEntityNames()

        assertNotNull(downstreamEntities)
        assertTrue(downstreamEntities.isEmpty())
    }
}
