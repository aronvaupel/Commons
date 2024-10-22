package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.kafka.DynamicTopicRegistration
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(EntityManagerFactory::class)
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