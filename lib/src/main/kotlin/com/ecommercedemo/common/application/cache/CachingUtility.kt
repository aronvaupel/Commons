package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.exception.NullKeyInZSetException
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

@Service
class CachingUtility(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) {
    private val log = KotlinLogging.logger {}

    fun save(redisKey: String, value: ByteArray) {
        redisTemplate.execute { connection ->
            connection.commands().set(redisKey.toByteArray(), value)
        }

        redisTemplate.opsForZSet().add("ranking", redisKey, System.currentTimeMillis().toDouble())
    }

    fun updateMemoryUsage(memoryData: Triple<Long, Long, Long>) {
        val updatedMemoryUsage = memoryData.first + memoryData.second - memoryData.third
        redisTemplate.opsForValue().set("total-memory-usage", updatedMemoryUsage.toString())
    }

    fun calculateMemoryUsageAndEvictIfNeeded(
        redisKey: String,
        data: ByteArray,
        maxMemory: Long
    ): Triple<Long, Long, Long> {
        val newEntryMemoryUsage = calculateMemoryUsageOfNewEntry(redisKey, data)
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

    private fun calculateMemoryUsageOfNewEntry(key: String, data: ByteArray): Long {
        val keySize = key.length.toLong()
        val valueSize = data.size.toLong()
        val zSetEntrySize = key.length + 8L
        return keySize + zSetEntrySize + valueSize
    }


    // Todo: is this even necessary? Take a look at the bean in RedisConfig
    fun <T : BaseEntity> serializeSearchResultToBytes(result: List<T>): ByteArray {
        println("Serializing search result to bytes")
        val uuids = result.map { it.id }
        val final = ByteBuffer.allocate(uuids.size * 16).apply {
            uuids.forEach { uuid ->
                putLong(uuid.mostSignificantBits)
                putLong(uuid.leastSignificantBits)
            }
        }.array()
        println("Serialized search result to bytes: $final")
        return final
    }


    fun deserializeSearchResultFromBytes(data: ByteArray): List<UUID> {
        println("Deserializing search result from bytes")
        val byteBuffer = ByteBuffer.wrap(data)
        val uuids = mutableListOf<UUID>()
        while (byteBuffer.remaining() >= 16) {
            val mostSigBits = byteBuffer.long
            val leastSigBits = byteBuffer.long
            uuids.add(UUID(mostSigBits, leastSigBits))
        }
        println("Deserialized search result from bytes: $uuids")
        return uuids
    }


    fun hashSearchRequest(request: SearchRequest): String {
        val requestBytes = objectMapper.writeValueAsBytes(request)
        return hash(requestBytes)
    }

    private fun hash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(data)
        return digest.digest().joinToString("") { "%02x".format(it) }
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