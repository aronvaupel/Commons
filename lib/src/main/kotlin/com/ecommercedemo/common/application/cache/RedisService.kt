package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.cache.values.Microservice
import com.ecommercedemo.common.application.cache.values.TopicDetails
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
class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${cache.memory.max-size}")
    private val maxMemorySize: Long,
    @Value("\${spring.application.name}")
    private val serviceName: String
) {

    val log = KotlinLogging.logger {}

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
            redisTemplate.opsForValue().get("kafka-topic-registry") ?: "{}",
            KafkaTopicRegistry::class.java
        )
    }

    private fun saveKafkaRegistry(kafkaTopicRegistry: KafkaTopicRegistry) {
        redisTemplate.opsForValue().set("kafka-topic-registry", objectMapper.writeValueAsString(kafkaTopicRegistry))
    }

    fun saveMappings(mappings: Map<String, Any>) {
        val serializedMappings = objectMapper.writeValueAsString(mappings)
        val serializedSize = serializedMappings.toByteArray().size.toLong()

        val metadataSize = "totalMemoryUsageInBytes".toByteArray().size + Long.SIZE_BYTES
        val totalMemoryUsage = serializedSize + metadataSize

        val mappingsAndMemoryUsage = mappings.toMutableMap().apply {
            put("totalMemoryUsageInBytes", totalMemoryUsage)
        }

        val enhancedSerializedData = objectMapper.writeValueAsString(mappingsAndMemoryUsage)
        redisTemplate.opsForValue().set("service-mappings", enhancedSerializedData)
    }



    //Todo: what about pseudo property paths?
    fun getCachedSearchResultsOrNullList(
        searchRequest: SearchRequest,
        entityName: String
    ): List<Pair<SearchParam, List<UUID>>?> {
        val redisKeys = searchRequest.params.map { param ->
            val fieldName = param.path.substringAfterLast(".")
            val hashedKey = generateCacheKey(param)
            "entities:$entityName:$fieldName:$hashedKey"
        }

        val results = redisTemplate.executePipelined { connection ->
            redisKeys.forEach { redisKey ->
                connection.stringCommands().get(redisKey.toByteArray())
            }
        }

        return searchRequest.params.mapIndexed { index, param ->
            val cachedEntry = results[index]?.let { result ->
                objectMapper.readValue(result.toString(), object : TypeReference<Map<String, Any>>() {})
            }

            val cachedIds = cachedEntry?.get("results") as? List<UUID>
            if (cachedIds != null) {
                val updatedEntry = cachedEntry.toMutableMap().apply {
                    put("lastAccess", System.currentTimeMillis())
                }
                redisTemplate.opsForValue().set(redisKeys[index], objectMapper.writeValueAsString(updatedEntry))
                param to cachedIds
            } else null
        }
    }

    fun overwriteSearchResults(
        entityName: String,
        searchRequest: SearchRequest,
        resultIds: List<UUID>
    ) {
        val rankingKey = "ranking:searches"

        val newEntrySize = calculateMemoryUsageOfNewEntry(resultIds)
        val currentTotalMemory = redisTemplate.opsForValue().get("totalMemoryUsageInBytes")?.toLong() ?: 0L

        val requiredMemory = currentTotalMemory + newEntrySize

        if (requiredMemory > maxMemorySize) {
            val memoryToFree = requiredMemory - maxMemorySize
            evictOldestEntries(rankingKey, memoryToFree)
        }

        searchRequest.params.forEach { param ->
            val hashedKey = generateCacheKey(param)
            val fieldName = param.path.substringAfterLast(".")
            val redisKey = "entities:$entityName:$fieldName:$hashedKey"

            val cacheEntry = mapOf(
                "lastAccess" to System.currentTimeMillis(),
                "memoryUsageInBytes" to newEntrySize,
                "results" to resultIds
            )

            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(cacheEntry))
        }

        val updatedTotalMemory = redisTemplate.opsForValue().get("totalMemoryUsageInBytes")?.toLong() ?: 0L
        redisTemplate.opsForValue().set("totalMemoryUsageInBytes", (updatedTotalMemory + newEntrySize).toString())
    }

    private fun evictOldestEntries(rankingKey: String, memoryToFree: Long) {
        var freedMemory = 0L

        while (freedMemory < memoryToFree) {
            val oldestEntry = redisTemplate.opsForZSet().popMin(rankingKey)
            if (oldestEntry == null) {
                log.warn("Eviction failed: no entries left to evict.")
                break
            }
            val redisKey = oldestEntry.value?.toString()
            if (redisKey != null) {
                val entrySize = redisTemplate.opsForValue().get(redisKey)?.let {
                val cacheEntry = objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
                cacheEntry["memoryUsageInBytes"] as Long
            } ?: 0L
                redisTemplate.delete(redisKey)
                freedMemory += entrySize
                redisTemplate.opsForValue().increment("totalMemoryUsageInBytes", -entrySize)
            }
        }
        log.info("Evicted entries freeing $freedMemory bytes.")
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
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun combineCachedIds(cachedSearchKeysList: List<Pair<SearchParam, List<UUID>>?>): List<UUID> {
        return cachedSearchKeysList.filterNotNull()
            .map { it.second }
            .reduceOrNull { acc, ids -> acc.intersect(ids.toSet()).toList() } ?: emptyList()
    }

    fun invalidateCaches(
        entityName: String,
        id: UUID,
        fields: Set<String>,
        modificationType: ModificationType
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
