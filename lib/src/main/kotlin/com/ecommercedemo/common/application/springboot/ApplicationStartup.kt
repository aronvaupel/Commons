package com.ecommercedemo.common.application.springboot

import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.service.concretion.EntityScanner
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val entityScanner: EntityScanner,
) {
    //Todo: can this be replaced with init block?
    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
    }
}