package com.ecommercedemo.common.redis

import com.ecommercedemo.common.redis.values.KafkaRegistry
import com.ecommercedemo.common.redis.values.Microservice
import com.ecommercedemo.common.redis.values.TopicDetails
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {

    fun registerAsTopics(upstreamEntities: List<String>) {
        val kafkaRegistry = getKafkaRegistry()
        upstreamEntities.forEach { entity ->
            val topicDetails = kafkaRegistry.topics[entity]
            when {
                topicDetails == null -> kafkaRegistry.topics[entity] = TopicDetails(
                    Microservice(serviceName, 1),
                    mutableSetOf()
                )

                topicDetails.producer.name == serviceName -> topicDetails.producer.instanceCount += 1

                else -> throw Exception(
                    "Topic $entity is already registered by another service. Only one source of truth is allowed"
                )

            }
        }
        save(kafkaRegistry)
    }

    fun deregisterProducer(serviceName: String) {
        val kafkaTopics = getKafkaRegistry()
        kafkaTopics.topics.entries.removeIf { (_, topicDetails) ->
            if (topicDetails.producer.name == serviceName) {
                --topicDetails.producer.instanceCount == 0
            } else false
        }
        save(kafkaTopics)
    }

    fun registerConsumer(downstreamEntity: String) {
        val kafkaTopics = getKafkaRegistry()
        val topicDetails = kafkaTopics.topics[downstreamEntity]

        when {
            topicDetails == null -> throw Exception("Topic $downstreamEntity does not exist.")

            topicDetails.consumers.any { it.name == serviceName } -> {
                val consumer = topicDetails.consumers.first { it.name == serviceName }
                consumer.instanceCount += 1
            }

            else -> {
                topicDetails.consumers.add(Microservice(serviceName, 1))
            }
        }

        save(kafkaTopics)
    }

    fun getKafkaRegistry(): KafkaRegistry {
        return objectMapper.readValue(
            redisTemplate.opsForValue().get("kafka-topics") ?: "{}",
            KafkaRegistry::class.java
        )
    }

    private fun save(kafkaRegistry: KafkaRegistry) {
        redisTemplate.opsForValue().set("kafka-topics", objectMapper.writeValueAsString(kafkaRegistry))
    }

}
