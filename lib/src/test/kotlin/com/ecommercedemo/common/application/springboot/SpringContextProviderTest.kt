package com.ecommercedemo.common.application.springboot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext

class SpringContextProviderTest {

    private val mockApplicationContext: ApplicationContext = mock(ApplicationContext::class.java)

    @BeforeEach
    fun setUp() {
        val springContextProvider = SpringContextProvider()
        springContextProvider.setApplicationContext(mockApplicationContext)
    }

    @Test
    fun `applicationContext is set correctly`() {
        assertNotNull(SpringContextProvider.applicationContext, "ApplicationContext should not be null.")
        assertEquals(
            mockApplicationContext,
            SpringContextProvider.applicationContext,
            "ApplicationContext should be the one set via setApplicationContext."
        )
    }

    @Test
    fun `applicationContext allows bean retrieval`() {
        val mockBean = Any()
        `when`(mockApplicationContext.getBean(Any::class.java)).thenReturn(mockBean)

        val retrievedBean = SpringContextProvider.applicationContext.getBean(Any::class.java)
        assertNotNull(retrievedBean, "Retrieved bean should not be null.")
        assertEquals(mockBean, retrievedBean, "Retrieved bean should match the mock bean.")
    }

}
