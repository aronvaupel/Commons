package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.kafka.DynamicTopicRegistration
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

@Component
@ConditionalOnExpression("'\${spring.kafka.bootstrap-servers:}' != ''")
class ApplicationStartup(
    @Autowired private val dynamicTopicRegistration: DynamicTopicRegistration,
    @Autowired private val entityScanner: EntityScanner,
) {

    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
    }
}