package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.redis.RedisService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(EntityScanner::class)
class ApplicationStartup(
    @Autowired private val dynamicTopicRegistration: DynamicTopicRegistration,
    @Autowired private val entityScanner: EntityScanner,
    @Autowired private val redisService: RedisService
) {

    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)
        redisService.addKafkaTopicNames(upstreamEntityNames)
    }
}