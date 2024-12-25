package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.cache.values.Microservice
import com.ecommercedemo.common.application.cache.values.TopicDetails
import com.ecommercedemo.common.application.exception.NotCachedException
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
open class RedisService(
    private val cachingUtility: CachingUtility,
    @Value("\${cache.memory.max-size}") private val maxMemory: Long,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${spring.application.name}") private val serviceName: String
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
        return ObjectMapper().readValue(
            redisTemplate.opsForValue().get("kafka-topic-registry") ?: "{}", KafkaTopicRegistry::class.java
        )
    }

    private fun saveKafkaRegistry(kafkaTopicRegistry: KafkaTopicRegistry) {
        redisTemplate.opsForValue().set("kafka-topic-registry", ObjectMapper().writeValueAsString(kafkaTopicRegistry))
    }

    //Todo: what about pseudo property paths?
    fun getCachedSearchResultsOrNullList(
        searchRequest: SearchRequest, entityName: String
    ): List<Pair<SearchParam, List<UUID>>?> {
        val redisKeyPrefix = "search:$entityName"
        return searchRequest.params.map { param ->
            val paramHash = cachingUtility.generateSearchCacheKey(param)
            val redisKey = "$redisKeyPrefix:${param.path}:$paramHash"

            val existsInRanking = redisTemplate.opsForZSet().rank("ranking", redisKey) != null
            if (!existsInRanking) return@map null

            redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())

            val result =
                (redisTemplate.opsForValue().get(redisKey) as String)
                    .split(",")
                    .map { UUID.fromString(it.trim()) }
            Pair(param, result)
        }
    }

    fun <T>getCachedMethodResultOrThrow(methodName: String, args: List<Any?>, returnTypeReference: TypeReference<T> ): Any? {
        val redisKeyPrefix = "method:$methodName"
        val argsAsString = ObjectMapper().writeValueAsString(args)
        val hashedArgs = cachingUtility.generateMethodCacheKey(methodName, argsAsString)
        val redisKey = "$redisKeyPrefix:$hashedArgs"

        val existsInRanking = redisTemplate.opsForZSet().rank("ranking", redisKey) != null
        if (!existsInRanking) throw NotCachedException()

        redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())

        return ObjectMapper().readValue(redisTemplate.opsForValue().get(redisKey), returnTypeReference)
    }


    fun cachePartialSearchResult(
        entityName: String, searchParam: SearchParam, resultIds: List<UUID>
    ) {
        val redisKeyPrefix = "search:$entityName"
        val hashedParam = cachingUtility.generateSearchCacheKey(searchParam)
        val redisKey = "$redisKeyPrefix:${searchParam.path}:$hashedParam"
        val entry = resultIds.joinToString(",")

        val memoryData = cachingUtility.calculateMemoryUsageAndEvictIfNeeded(redisKey, entry, maxMemory)
        cachingUtility.save(redisKey, entry)
        cachingUtility.updateMemoryUsage(memoryData)
    }

    fun cacheMethodResult(methodName: String, args: List<Any?>, result: Any?) {
        val redisKeyPrefix = "method:$methodName"
        val argsAsString = ObjectMapper().writeValueAsString(args)
        val hashedArgs = cachingUtility.generateMethodCacheKey(methodName, argsAsString)
        val redisKey = "$redisKeyPrefix:$hashedArgs"
        val resultAsString = ObjectMapper().writeValueAsString(result)

        val memoryData = cachingUtility.calculateMemoryUsageAndEvictIfNeeded(redisKey, resultAsString, maxMemory)
        cachingUtility.save(redisKey, resultAsString)
        cachingUtility.updateMemoryUsage(memoryData)
    }

}
