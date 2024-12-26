package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.cache.values.Microservice
import com.ecommercedemo.common.application.cache.values.TopicDetails
import com.ecommercedemo.common.application.exception.NotCachedException
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
@DependsOn("objectMapper")
open class RedisService(
    private val cachingUtility: CachingUtility,
    @Value("\${cache.memory.max-size}") private val maxMemory: Long,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.application.name}") private val serviceName: String,
    private val objectMapper: ObjectMapper
) {

    val log = KotlinLogging.logger {}


    fun registerAsTopics(upstreamEntities: List<String>) {
        val kafkaRegistry = getKafkaRegistry()
        upstreamEntities.forEach { entity ->
            val topicDetails = kafkaRegistry.topics[entity]
            when {
                topicDetails == null -> kafkaRegistry.topics[entity] = TopicDetails(
                    Microservice(serviceName, 1), mutableSetOf()
                )
                //Fixme: this is broken
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
        val topicDetails = kafkaRegistry.topics[downstreamEntity.removePrefix("_")]

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
            redisTemplate.opsForValue().get("kafka-topic-registry") ?: "{}", KafkaTopicRegistry::class.java
        )
    }

    private fun saveKafkaRegistry(kafkaTopicRegistry: KafkaTopicRegistry) {
        redisTemplate.opsForValue().set("kafka-topic-registry", objectMapper.writeValueAsString(kafkaTopicRegistry))
    }

    private fun <T> cacheResult(
        keyPrefix: String,
        hashedIdentifier: String,
        result: T,
        serializationMethod: (T) -> ByteArray
    ) {
        val redisKey = "$keyPrefix:$hashedIdentifier"
        val entry = serializationMethod(result)

        val memoryData = cachingUtility.calculateMemoryUsageAndEvictIfNeeded(redisKey, maxMemory)
        cachingUtility.save(redisKey, entry)
        cachingUtility.updateMemoryUsage(memoryData)
    }

    fun <T : BaseEntity> cacheSearchResult(
        entityName: String,
        request: SearchRequest,
        searchResult: List<T>
    ) {
        cacheResult(
            keyPrefix = "search:$entityName",
            hashedIdentifier = cachingUtility.hashSearchRequest(request),
            result = searchResult,
            serializationMethod = cachingUtility::serializeSearchResultToBytes
        )
    }


    fun cacheMethodResult(
        methodName: String,
        args: List<Any?>,
        result: Any?
    ) {
        cacheResult(
            keyPrefix = "method:$methodName",
            hashedIdentifier = cachingUtility.hashArgs(args),
            result = result,
            serializationMethod = cachingUtility::serializeMethodResultToBytes
        )
    }

    private fun <T> getCachedResultOrThrow(
        keyPrefix: String,
        hashedIdentifier: String,
        deserializeFunction: (ByteArray) -> T
    ): T {
        val redisKey = "$keyPrefix:$hashedIdentifier"

        if (redisTemplate.opsForZSet().rank("ranking", redisKey) != null) {
            redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())
            val cachedValue = redisTemplate.execute { connection ->
                connection.stringCommands().get(redisKey.toByteArray())
            } ?: throw NotCachedException()
            return deserializeFunction(cachedValue)
        } else {
            throw NotCachedException()
        }
    }

    fun <T> getCachedMethodResultOrThrow(
        methodName: String,
        args: List<Any?>,
        returnTypeReference: TypeReference<T>
    ): T {
        return getCachedResultOrThrow(
            keyPrefix = "method:$methodName",
            hashedIdentifier = cachingUtility.hashArgs(args),
            deserializeFunction = { data -> cachingUtility.deserializeMethodResultFromBytes(data, returnTypeReference) }
        )
    }

    fun getCachedSearchResultsOrThrow(
        searchRequest: SearchRequest,
        entityName: String
    ): List<UUID> {
        return getCachedResultOrThrow(
            keyPrefix = "search:$entityName",
            hashedIdentifier = cachingUtility.hashSearchRequest(searchRequest),
            deserializeFunction = cachingUtility::deserializeSearchResultFromBytes
        )
    }

}
