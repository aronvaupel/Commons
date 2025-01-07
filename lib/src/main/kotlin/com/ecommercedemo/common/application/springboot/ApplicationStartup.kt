package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.service.concretion.RepositoryScanner
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val repositoryScanner: RepositoryScanner,
) {

    @PostConstruct
    fun init() {
        val upstreamEntityNames = repositoryScanner.getUpstreamRepositoryNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
    }
}