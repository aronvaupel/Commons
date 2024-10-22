package com.ecommercedemo.common.redis

import com.ecommercedemo.common.redis.keys.KafkaTopicRegistry
import com.ecommercedemo.common.redis.values.Microservice
import com.ecommercedemo.common.redis.values.TopicDetails
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
) {

    @ConditionalOnBean(EntityManagerFactory::class)
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
        saveKafkaRegistry(kafkaRegistry)
    }

    @ConditionalOnExpression("'\${spring.application.name}' == 'gateway-service'")
    @Suppress("unused")
    fun deregisterProducer(serviceName: String) {
        val kafkaTopics = getKafkaRegistry()
        kafkaTopics.topics.entries.removeIf { (_, topicDetails) ->
            if (topicDetails.producer.name == serviceName) {
                --topicDetails.producer.instanceCount == 0
            } else false
        }
        saveKafkaRegistry(kafkaTopics)
    }

    @ConditionalOnBean(EntityManagerFactory::class)
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

        saveKafkaRegistry(kafkaTopics)
    }

    fun getKafkaRegistry(): KafkaTopicRegistry {
        return objectMapper.readValue(
            redisTemplate.opsForValue().get("kafka-topic-registry") ?: "{}",
            KafkaTopicRegistry::class.java
        )
    }

    private fun saveKafkaRegistry(kafkaTopicRegistry: KafkaTopicRegistry) {
        redisTemplate.opsForValue().set("kafka-topic-registry", objectMapper.writeValueAsString(kafkaTopicRegistry))
    }

}
