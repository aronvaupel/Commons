package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.kafka.DynamicTopicRegistration
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val entityScanner: EntityScanner,
) {
    init {
        println("ApplicationStartup bean created")
    }

    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
    }
}