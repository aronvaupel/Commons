package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.service.DiscoveryService
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import kotlin.test.Test


class ApplicationStartupTest {

    private lateinit var dynamicTopicRegistration: DynamicTopicRegistration
    private lateinit var repositoryScanner: RepositoryScanner
    private lateinit var serviceLevelAccess: ServiceLevelAccess
    private lateinit var discoveryService: DiscoveryService

    private lateinit var applicationStartup: ApplicationStartup

    @BeforeEach
    fun setUp() {
        dynamicTopicRegistration = mock(DynamicTopicRegistration::class.java)
        repositoryScanner = mock(RepositoryScanner::class.java)
        serviceLevelAccess = mock(ServiceLevelAccess::class.java)
        discoveryService = mock(DiscoveryService::class.java)

        applicationStartup = ApplicationStartup(
            dynamicTopicRegistration,
            repositoryScanner,
            serviceLevelAccess,
            discoveryService
        )
    }

    @Test
    fun `init should call the appropriate methods on dependencies`() {
        val mockEntityNames = listOf("Entity1", "Entity2")
        val mockEndpointMetadata = listOf(
            EndpointMetadata(
                path = "/api/v1/resource",
                method = "GET",
                roles = setOf("ROLE_USER"),
                pathVariables = emptyList(),
                requestParameters = emptyList()
            ),
            EndpointMetadata(
                path = "/api/v1/resource/{id}",
                method = "POST",
                roles = setOf("ROLE_ADMIN"),
                pathVariables = listOf(
                    EndpointMethodParam(
                        name = "id",
                        position = 0,
                        typeSimpleName = "String"
                    )
                ),
                requestParameters = emptyList()
            )
        )

        `when`(repositoryScanner.getUpstreamEntityNames()).thenReturn(mockEntityNames)
        `when`(discoveryService.extractEndpointMetadata()).thenReturn(mockEndpointMetadata)

        applicationStartup.init()

        verify(repositoryScanner).getUpstreamEntityNames()
        verify(dynamicTopicRegistration).declareKafkaTopics(mockEntityNames)
        verify(discoveryService).extractEndpointMetadata()
        verify(discoveryService).registerEndpointInfoToEureka(mockEndpointMetadata)
    }
}


