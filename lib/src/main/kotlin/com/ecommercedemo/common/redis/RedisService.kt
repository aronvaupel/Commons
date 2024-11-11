package com.ecommercedemo.common.redis

import com.ecommercedemo.common.redis.keys.KafkaTopicRegistry
import com.ecommercedemo.common.redis.values.Microservice
import com.ecommercedemo.common.redis.values.TopicDetails
import com.ecommercedemo.common.util.filter.QueryParams
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Service
class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.application.name}")
    private val serviceName: String
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
        saveKafkaRegistry(kafkaRegistry)
    }

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

    fun registerConsumer(downstreamEntity: String) {
        val kafkaRegistry = getKafkaRegistry()
        val topicDetails = kafkaRegistry.topics[downstreamEntity]

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

        saveKafkaRegistry(kafkaRegistry)
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

    fun cacheMetadata(key: String, value: Any) {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), 24, TimeUnit.HOURS)
    }

    fun getMetadata(key: String): Any? {
        val cachedData = redisTemplate.opsForValue().get(key) ?: return null
        return objectMapper.readValue(cachedData, Any::class.java)
    }

    fun cacheQueryResult(key: String, value: List<Any>) {
        redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), 10, TimeUnit.MINUTES)
    }

    fun getQueryResult(key: String): List<Any>? {
        val cachedData = redisTemplate.opsForValue().get(key) ?: return null
        return objectMapper.readValue(cachedData, object : TypeReference<List<Any>>() {})
    }

    fun <T: Any>generateQueryKey(entityClass: Class<T>, queryParameters: QueryParams<T>): String {
        val hashSource = entityClass::class.java.simpleName + queryParameters.toString()
        return "query:${hashSource.hashCode()}"
    }

    fun <T: Any>generateMetadataKey(entityClass: KClass<T>, attribute: KProperty1<T, *>): String {
        return "metadata:${entityClass.simpleName}:${attribute.name}"
    }

}
