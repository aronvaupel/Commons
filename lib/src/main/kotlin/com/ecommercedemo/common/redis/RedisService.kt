package com.ecommercedemo.common.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun createKafkaTopicNames(entityNames: List<String>) {
        println("Setting Kafka topic names in Redis")
        redisTemplate.opsForValue().set("kafka-topic-names", entityNames)
    }
}