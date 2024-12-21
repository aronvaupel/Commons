package com.ecommercedemo.common.application

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.kafka.DynamicTopicRegistration
import com.ecommercedemo.common.service.concretion.EntityScanner
import com.ecommercedemo.common.service.concretion.ServiceMapper
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
class ApplicationStartup @Autowired constructor(
    private val dynamicTopicRegistration: DynamicTopicRegistration,
    private val entityScanner: EntityScanner,
    private val serviceMapper: ServiceMapper,
    private val redisService: RedisService
) {
    //Todo: can this be replaced with init block?
    @PostConstruct
    fun init() {
        val upstreamEntityNames = entityScanner.getUpstreamEntityNames()
        dynamicTopicRegistration.declareKafkaTopics(upstreamEntityNames)

        val mappings = serviceMapper.mapEntities()
        redisService.saveMappings(mappings)
    }
}