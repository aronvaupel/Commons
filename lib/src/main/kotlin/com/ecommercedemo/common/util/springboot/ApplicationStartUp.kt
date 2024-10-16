package com.ecommercedemo.common.util.springboot

import com.ecommercedemo.common.redis.RedisService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ApplicationStartup(
    @Autowired private val redisService: RedisService,
    @Autowired private val entityScanner: EntityScanner
) {

    @PostConstruct
    fun init() {
        println("Scanning for entities...")
        val entityNames = entityScanner.getEntityNames()
        println("Adding Kafka topic names to Redis...")
        redisService.addKafkaTopicNames(entityNames)
        println("Entities scanned and Kafka topic names added to Redis.")
    }
}