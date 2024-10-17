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


    fun addKafkaTopicNames(upstreamEntityNames: List<String>) {
        redisTemplate.execute { connection ->
            connection.multi()
            val existingTopicNames = redisTemplate.opsForList().range("kafka-topic-names", 0, -1) ?: mutableListOf()
            upstreamEntityNames.forEach { topicName ->
                if (!existingTopicNames.contains(topicName)) {
                    redisTemplate.opsForList().rightPush("kafka-topic-names", topicName)
                } else throw IllegalArgumentException("Topic name $topicName already exists in Redis.")
            }

            connection.exec()
        }
    }

    fun isRegistrationComplete(): Boolean {
        return redisTemplate.opsForValue().get("kafka-topic-names-complete") == "true"
    }
}