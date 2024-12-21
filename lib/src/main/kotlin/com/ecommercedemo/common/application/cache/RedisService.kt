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
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Duration
import java.util.*

@Service
@Suppress("UNCHECKED_CAST")
open class RedisService(
    @Autowired private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate,
    @Value("\${cache.memory.max-size}") private val maxMemorySize: Long,
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

    fun saveApplicationMap(mappings: Map<String, Any>) {
        val serializedMappings = objectMapper.writeValueAsString(mappings)
        val serializedSize = serializedMappings.toByteArray().size.toLong()

        val metadataSize = "total-memory-usage-in-bytes".toByteArray().size + Long.SIZE_BYTES
        val totalMemoryUsage = serializedSize + metadataSize

        val mappingsAndMemoryUsage = mutableMapOf<String, Any>().apply {
            put("entities", mappings)
            put("total-memory-usage-in-bytes", totalMemoryUsage)
            put("eviction-candidates", listOf(mapOf<String, Long>()))
        }

        val enhancedSerializedData = objectMapper.writeValueAsString(mappingsAndMemoryUsage)
        redisTemplate.opsForValue().set("application-map", enhancedSerializedData)
    }

    private fun getMappings(): Map<String, Any>? {
        val serializedMappings = redisTemplate.opsForValue().get("application-map")
        return serializedMappings?.let {
            objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
        }
    }

    //Todo: what about pseudo property paths?
    fun getCachedSearchResultsOrNullList(
        searchRequest: SearchRequest, entityName: String
    ): List<Pair<SearchParam, List<UUID>>?> {
        val redisKeys = searchRequest.params.map { param ->
            val fieldName = param.path.substringAfterLast(".")
            val hashedKey = generateCacheKey(param)
            "entities:$entityName:$fieldName:$hashedKey"
        }

        val results = redisTemplate.executePipelined { connection ->
            val stringCommands = connection.stringCommands()
            redisKeys.forEach { redisKey ->
                stringCommands.get(redisKey.toByteArray())
            }
            null
        }
        return searchRequest.params.mapIndexed { index, param ->
            val cachedEntry = results[index]?.let { result ->
                objectMapper.readValue(result.toString(), object : TypeReference<Map<String, Any>>() {})
            }

            val cachedIds = cachedEntry?.get("results") as? List<UUID>
            if (cachedIds != null) {
                val updatedEntry = cachedEntry.toMutableMap().apply {
                    put("last-access", System.currentTimeMillis())
                }
                redisTemplate.opsForValue().set(redisKeys[index], objectMapper.writeValueAsString(updatedEntry))
                param to cachedIds
            } else null
        }
    }

    fun overwriteSearchResults(
        entityName: String, searchRequest: SearchRequest, resultIds: List<UUID>
    ) {
        val newEntrySize = calculateMemoryUsageOfNewEntry(resultIds)
        var currentTotalMemory = redisTemplate.opsForValue().get("total-memory-usage-in-bytes")?.toLong() ?: 0L
        val excess = currentTotalMemory + newEntrySize - maxMemorySize

        if (excess > 0) {
            currentTotalMemory -= evictOldestEntries(excess)
            refreshEvictionCandidates()
        }

        searchRequest.params.forEach { param ->
            val hashedKey = generateCacheKey(param)
            val fieldName = param.path.substringAfterLast(".")
            val redisKey = "entities:$entityName:$fieldName:$hashedKey"

            val cacheEntry = mapOf(
                "last-access" to System.currentTimeMillis(),
                "memory-usage-in-bytes" to newEntrySize,
                "results" to resultIds
            )
            redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(cacheEntry))
            redisTemplate.opsForValue().set("total-memory-usage-in-bytes", (currentTotalMemory + newEntrySize).toString())

            val numberEvictionCandidates = redisTemplate.opsForValue()
                        .get("eviction-candidates")?.let {
                            objectMapper.readValue(it, object : TypeReference<List<Map<String, Long>>>() {})
                        }?.size ?: 0
            if (numberEvictionCandidates < 100)
                redisTemplate.opsForZSet().add(
                    "eviction-candidates",
                    redisKey,
                    System.currentTimeMillis().toDouble()
                )
        }
    }

    private fun evictOldestEntries(excess: Long): Long {
        var freedMemory = 0L
        val candidateKeysToEvict = mutableListOf<String>()

        val leastRecentCandidates =
            redisTemplate.opsForZSet().rangeWithScores("eviction-candidates", 0, -1) ?: emptySet()


        for (candidate in leastRecentCandidates) {
            if (freedMemory >= excess) break

            val redisKey = candidate.value?.toString()
            val memoryUsageInBytes = redisKey?.let { key ->
                redisTemplate.opsForValue().get(key)?.let {
                    val cacheEntry = objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
                    cacheEntry["memory-usage-in-bytes"] as? Long ?: 0L
                }
            } ?: continue

            candidateKeysToEvict.add(redisKey)
            freedMemory += memoryUsageInBytes
        }

        candidateKeysToEvict.forEach { redisKey ->
            redisTemplate.delete(redisKey)
            redisTemplate.opsForZSet().remove("eviction-candidates", redisKey)
        }

        if (freedMemory < excess) {
            val remainingKeys = redisTemplate.keys("entities:*") ?: emptySet()

            var additionalFreedMemory = 0L
            val keysToEvict = remainingKeys.mapNotNull { key ->
                if (additionalFreedMemory >= excess - freedMemory) return@mapNotNull null
                val cachedEntry = redisTemplate.opsForValue().get(key)?.let {
                    objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
                }
                val lastAccess = cachedEntry?.get("last-access") as? Long
                additionalFreedMemory += cachedEntry?.get("memory-usage-in-bytes") as? Long ?: 0L
                key to lastAccess
            }.sortedBy { it.second }

            redisTemplate.delete(keysToEvict.map { it.first })
            refreshEvictionCandidates()
        }

        redisTemplate.opsForValue().increment("total-memory-usage-in-bytes", -freedMemory)
        log.info("Evicted entries freeing $freedMemory bytes.")
        return freedMemory
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

    fun combineCachedIds(cachedSearchKeysList: List<Pair<SearchParam, List<UUID>>?>): List<UUID> {
        return cachedSearchKeysList.filterNotNull().map { it.second }
            .reduceOrNull { acc, ids -> acc.intersect(ids.toSet()).toList() } ?: emptyList()
    }

    fun invalidateCaches(
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

    @Async
    open fun refreshEvictionCandidates() {
        val lockKey = "refresh-eviction-candidates-lock"
        val lockExpiration = 60L
        val maxRetries = 5
        val retryDelayMillis = 12000L
        var retries = 0

        while (retries < maxRetries) {
            val lockValue = UUID.randomUUID().toString()
            val lockAcquired =
                redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(lockExpiration))

            if (lockAcquired == true) {
                try {
                    performEvictionCandidateRefresh()
                    return
                } catch (e: Exception) {
                    log.error("Error refreshing least recent candidates", e)
                } finally {
                    val currentLockValue = redisTemplate.opsForValue().get(lockKey)
                    if (lockValue == currentLockValue) {
                        redisTemplate.delete(lockKey)
                    }
                }
            } else {
                retries++
                Thread.sleep(retryDelayMillis)
            }
        }
    }


    private fun performEvictionCandidateRefresh() {
        val mappings = getMappings()?.toMutableMap() ?: throw Exception("Mappings not found")
        val currentCandidates = mappings["eviction-candidates"] as? List<Map<String, Long>> ?: emptyList()
        val currentCandidateKeys = currentCandidates.map { it.keys.first() }.toSet()

        val allKeysInCache = redisTemplate.keys("entities:*") ?: emptySet()
        val potentialNewCandidates = allKeysInCache - currentCandidateKeys

        val candidates = redisTemplate.executePipelined { connection ->
            potentialNewCandidates.forEach { key ->
                connection.stringCommands().get(key.toByteArray())
            }
            null
        }.mapIndexedNotNull { index, result ->
            val key = potentialNewCandidates.elementAt(index)
            val cachedEntry = result?.toString()?.let {
                objectMapper.readValue(it, object : TypeReference<Map<String, Any>>() {})
            }
            val lastAccess = cachedEntry?.get("last-access") as? Long
            val memoryUsageInBytes = cachedEntry?.get("memory-usage-in-bytes") as? Long
            if (lastAccess != null && memoryUsageInBytes != null) {
                key to (lastAccess to memoryUsageInBytes)
            } else null
        }

        val combinedCandidates = (currentCandidates.map {
            val entry = it.entries.first()
            entry.key to (entry.value to 0L)
        } + candidates).sortedBy { it.second.first }.take(100)

        val updatedCandidateList = combinedCandidates.map { (key, data) ->
            mapOf(key to data.second)
        }

        mappings["eviction-candidates"] = updatedCandidateList
        saveApplicationMap(mappings)
    }

}
