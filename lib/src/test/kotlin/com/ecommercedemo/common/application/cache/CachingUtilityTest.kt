package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.core.ZSetOperations
import java.util.*

class CachingUtilityTest {

    private lateinit var cachingUtility: CachingUtility
    private lateinit var redisTemplate: StringRedisTemplate
    private lateinit var objectMapper: ObjectMapper
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var zSetOperations: ZSetOperations<String, String>

    @BeforeEach
    fun setUp() {
        redisTemplate = mock(StringRedisTemplate::class.java)
        objectMapper = mock(ObjectMapper::class.java)
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        zSetOperations = mock(ZSetOperations::class.java) as ZSetOperations<String, String>

        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        `when`(redisTemplate.opsForZSet()).thenReturn(zSetOperations)

        cachingUtility = CachingUtility(redisTemplate, objectMapper)
    }

    @Test
    fun `should save data to Redis`() {
        val redisKey = "testKey"
        val value = byteArrayOf(1, 2, 3)

        cachingUtility.save(redisKey, value)

        verify(zSetOperations).add("ranking", redisKey, System.currentTimeMillis().toDouble())
    }

    @Test
    fun `should calculate memory usage and evict entries if needed`() {
        val redisKey = "testKey"
        val data = byteArrayOf(1, 2, 3)
        `when`(valueOperations.get("total-memory-usage")).thenReturn("1024")

        val result = cachingUtility.calculateMemoryUsageAndEvictIfNeeded(redisKey, data, 2048L)

        assertNotNull(result)
        assertEquals(1024L, result.first)
    }

    @Test
    fun `should evict oldest entries to free memory`() {
        val excess = 500L
        val redisKey = "testKey"
        `when`(zSetOperations.rangeWithScores("ranking", 0, -1)).thenReturn(setOf())

        val freedMemory = cachingUtility.calculateMemoryUsageAndEvictIfNeeded(redisKey, byteArrayOf(), excess)

        assertEquals(0L, freedMemory.third)
    }

    @Test
    fun `should serialize and deserialize search results`() {
        class TestEntity(override var id: UUID) : BaseEntity()

        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val entity1 = TestEntity(uuid1)
        val entity2 = TestEntity(uuid2)

        val searchResults = listOf(entity1, entity2)

        val serialized = cachingUtility.serializeSearchResultToBytes(searchResults)
        val deserialized = cachingUtility.deserializeSearchResultFromBytes(serialized)

        assertEquals(listOf(uuid1, uuid2), deserialized)
    }

    @Test
    fun `should hash search request`() {
        val searchRequest = SearchRequest()
        val requestBytes = byteArrayOf(1, 2, 3)

        `when`(objectMapper.writeValueAsBytes(searchRequest)).thenReturn(requestBytes)

        val hash = cachingUtility.hashSearchRequest(searchRequest)

        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())
    }

    @Test
    fun `should invalidate search caches on modification`() {
        val entityName = "TestEntity"
        val fields = setOf("field1", "field2")
        `when`(redisTemplate.keys("entities:$entityName:field1:*")).thenReturn(setOf("key1"))
        `when`(redisTemplate.keys("entities:$entityName:field2:*")).thenReturn(setOf("key2"))

        cachingUtility.invalidateSearchCaches(entityName, UUID.randomUUID(), fields, ModificationType.UPDATE)

        verify(redisTemplate).delete("key1")
        verify(redisTemplate).delete("key2")
    }

}
