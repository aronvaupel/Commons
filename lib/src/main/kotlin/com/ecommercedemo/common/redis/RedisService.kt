package com.ecommercedemo.common.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(
    private val redisTemplate: StringRedisTemplate
) {

    fun getKafkaTopicNames(): List<String> {
        return redisTemplate.opsForList().range("kafka-topic-names", 0, -1) ?: emptyList()
    }

    fun addKafkaTopicNames(topicNames: List<String>) {
        redisTemplate.execute { connection ->
            connection.multi()
            val existingTopicNames = redisTemplate.opsForList().range("kafka-topic-names", 0, -1) ?: mutableListOf()

            topicNames.forEach { topicName ->
                if (!topicName.contains("downstream", ignoreCase = true) && topicName != "CustomProperty" && !existingTopicNames.contains(topicName)) {
                    redisTemplate.opsForList().rightPush("kafka-topic-names", topicName)
                }
            }

            connection.exec()
        }
    }
}