package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.exception.NullKeyInZSetException
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.*

@Service
class CachingUtility(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = KotlinLogging.logger {}

    fun save(redisKey: String, entry: String) {
        redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(entry))

        redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())
    }

    fun updateMemoryUsage(memoryData: Triple<Long, Long, Long>) {
        val updatedMemoryUsage = memoryData.first + memoryData.second - memoryData.third
        redisTemplate.opsForValue().set("total-memory-usage", updatedMemoryUsage.toString())
    }

    fun calculateMemoryUsageAndEvictIfNeeded(
        redisKey: String,
        resultAsString: String,
        maxMemory: Long
    ): Triple<Long, Long, Long> {
        val newEntryMemoryUsage = calculateMemoryUsageOfNewEntry(redisKey, resultAsString)
        val currentMemoryUsage = redisTemplate.opsForValue().get("total-memory-usage")?.toLong() ?: 0L

        val excess = (currentMemoryUsage + newEntryMemoryUsage) - maxMemory
        val freedMemory = if (excess > 0) evictOldestEntries(excess) else 0L

        return Triple(currentMemoryUsage, newEntryMemoryUsage, freedMemory)
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


    private fun calculateMemoryUsageOfNewEntry(key: String, value: String): Long {
        val stringSize = (key.length + value.length).toLong()
        val zSetEntrySize = key.length + 8L
        return stringSize + zSetEntrySize
    }

    fun generateSearchCacheKey(param: SearchParam): String {
        return hash("${param.operator.name}:${param.searchValue}")
    }

    fun generateMethodCacheKey(methodName: String, argsAsString: String): String {
        return hash("method:$methodName:$argsAsString")
    }

    fun generateMethodCacheKey(methodName: String, args: Array<Any?>): String {
        val argsHash = args.joinToString(",") { it.toString() }
            .toByteArray()
            .let { MessageDigest.getInstance("SHA-256").digest(it) }
            .joinToString("") { "%02x".format(it) }

        return "method:$methodName:$argsHash"
    }
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    //Todo: consider moving to RestServiceTemplate
    fun resultIntersection(cachedSearchSearchResultsOrNullList: List<Pair<SearchParam, List<UUID>>?>): List<UUID> {
        return cachedSearchSearchResultsOrNullList.filterNotNull().map { it.second }
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


    fun invalidateWhenModified(entityName: String, modifiedFields: Set<String>) {
        modifiedFields.forEach { fieldName ->
            val fieldKeys = redisTemplate.keys("entities:$entityName:$fieldName:*")
            fieldKeys.forEach { key ->
                println("Invalidating key: $key")
                redisTemplate.delete(key)
            }
        }
    }

    fun invalidateWhenDeleted(entityName: String, entityId: UUID) {
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