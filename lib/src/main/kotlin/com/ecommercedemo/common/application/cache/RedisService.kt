package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.cache.values.Microservice
import com.ecommercedemo.common.application.cache.values.TopicDetails
import com.ecommercedemo.common.application.exception.NullKeyInZSetException
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
@Suppress("UNCHECKED_CAST")
open class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${cache.memory.max-size}") private val maxMemory: Long,
    @Value("\${spring.application.name}") private val serviceName: String
) {

    val log = KotlinLogging.logger {}

    //Todo: use the other mapping
    fun registerAsTopics(upstreamEntities: List<String>) {
        val kafkaRegistry = getKafkaRegistry()
        upstreamEntities.forEach { entity ->
            val topicDetails = kafkaRegistry.topics[entity]
            when {
                topicDetails == null -> kafkaRegistry.topics[entity] = TopicDetails(
                    Microservice(serviceName, 1), mutableSetOf()
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

    //Todo: what about pseudo property paths?
    fun getCachedSearchResultsOrNullList(
        searchRequest: SearchRequest, entityName: String
    ): List<Pair<SearchParam, List<UUID>>?> {
        val redisKeyPrefix = "search:$entityName"
        return searchRequest.params.map { param ->
            val paramHash = generateCacheKey(param)
            val redisKey = "$redisKeyPrefix:${param.path}:$paramHash"

            val existsInRanking = redisTemplate.opsForZSet().rank("ranking", redisKey) != null
            if (!existsInRanking) return@map null

            redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())

            val entry = objectMapper.readValue(
                redisTemplate.opsForValue().get(redisKey),
                object : TypeReference<Map<String, Any>>() {})
            val result = entry["result"] as List<UUID>
            Pair(param, result)
        }
    }


    fun overwriteSearchResults(
        entityName: String, searchRequest: SearchRequest, resultIds: List<UUID>
    ) {
        val redisKeyPrefix = "search:$entityName"
        val newEntryMemoryUsage = calculateMemoryUsageOfNewEntry(resultIds)
        val currentMemoryUsage = redisTemplate.opsForValue().get("total-memory-usage")?.toLong() ?: 0L

        val excess = (currentMemoryUsage + newEntryMemoryUsage) - maxMemory
        var freedMemory = 0L
        if (excess > 0) freedMemory = evictOldestEntries(excess)

        searchRequest.params.forEach { param ->
            val hashedParam = generateCacheKey(param)
            val redisKey = "$redisKeyPrefix:${param.path}:$hashedParam"

            val entry =
                mapOf(
                    "memoryUsage" to newEntryMemoryUsage.toString(),
                    "result" to resultIds
                )

            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(entry))

            redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())
        }

        val updatedMemoryUsage = currentMemoryUsage + newEntryMemoryUsage - freedMemory
        redisTemplate.opsForValue().set("total-memory-usage", updatedMemoryUsage.toString())
    }


    private fun evictOldestEntries(excess: Long): Long {
        var memoryFreed = 0L
        val zSetKey = "ranking"

        redisTemplate.opsForZSet()
            .rangeWithScores(zSetKey, 0, -1)
            ?.iterator()?.run {
                while (this.hasNext() && memoryFreed < excess) {
                    val entry = this.next()
                    val key = entry.value ?: throw NullKeyInZSetException("Null key in zSet", zSetKey, entry)

                    val memoryUsage = redisTemplate.opsForValue().get(key)?.let {
                        objectMapper.readValue(
                            it,
                            object : TypeReference<Map<String, Any>>() {})["memoryUsage"] as? Long ?: 0L
                    } ?: 0L
                    redisTemplate.delete(key)
                    redisTemplate.opsForZSet().remove(zSetKey, key)

                    memoryFreed += memoryUsage
                }
            } ?: log.info { "No entries to evict. Consider allocating more memory to the cache" }

        return memoryFreed
    }


    private fun calculateMemoryUsageOfNewEntry(resultIds: List<UUID>): Long {
        val hashedKeySize = 64L
        val metadataSize = 16L
        val resultsSize = resultIds.size * 36L
        val serializationOverhead = (hashedKeySize + metadataSize + resultsSize) / 10 //approximation

        return hashedKeySize + metadataSize + resultsSize + serializationOverhead
    }

    private fun generateCacheKey(param: SearchParam): String {
        return hash("${param.operator.name}:${param.searchValue}")
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    //Todo: consider moving to RestServiceTemplate
    fun resultIntersection(cachedSearchKeysList: List<Pair<SearchParam, List<UUID>>?>): List<UUID> {
        return cachedSearchKeysList.filterNotNull().map { it.second }
            .reduceOrNull { acc, ids -> acc.intersect(ids.toSet()).toList() } ?: emptyList()
    }

    fun invalidateSearchCaches(
        entityName: String, id: UUID, fields: Set<String>, modificationType: ModificationType
    ) {
        when (modificationType) {
            ModificationType.CREATE, ModificationType.UPDATE -> invalidateWhenModified(entityName, fields)
            ModificationType.DELETE -> invalidateWhenDeleted(entityName, id)
        }
    }


    private fun invalidateWhenModified(entityName: String, modifiedFields: Set<String>) {
        modifiedFields.forEach { fieldName ->
            val fieldKeys = redisTemplate.keys("entities:$entityName:$fieldName:*")
            fieldKeys.forEach { key ->
                println("Invalidating key: $key")
                redisTemplate.delete(key)
            }
        }
    }

    private fun invalidateWhenDeleted(entityName: String, entityId: UUID) {
        val keysToCheck = redisTemplate.keys("entities:$entityName:*")
        keysToCheck.forEach { key ->
            val cachedIds = redisTemplate.opsForValue().get(key)?.let {
                objectMapper.readValue(it, object : TypeReference<List<UUID>>() {})
            }
            if (cachedIds != null) {
                val updatedIds = cachedIds.filterNot { it == entityId }
                if (updatedIds.isEmpty()) redisTemplate.delete(key)
                else redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(updatedIds))
            }
        }
    }

}
