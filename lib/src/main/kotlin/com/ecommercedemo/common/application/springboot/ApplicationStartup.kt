package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.service.DiscoveryService
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val repositoryScanner: RepositoryScanner,
    private val serviceLevelAccess: ServiceLevelAccess,
    private val discoveryService: DiscoveryService
) {

    @PostConstruct
    fun init() {
        println("SERVICE LEVEL RESTRICTIONS: $serviceLevelAccess")
        val upstreamEntityNames = repositoryScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
        val enrichedEndpointMetadata = discoveryService.extractEndpointMetadata()
        discoveryService.registerEndpointInfoToEureka(enrichedEndpointMetadata)
    }

}