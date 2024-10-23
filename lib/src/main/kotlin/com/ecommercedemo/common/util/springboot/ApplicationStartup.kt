package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.kafka.DynamicTopicRegistration
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val entityScanner: EntityScanner,
) {

    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
    }
}