package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.application.cache.keys.KafkaTopicRegistry
import com.ecommercedemo.common.application.cache.values.Microservice
import com.ecommercedemo.common.application.cache.values.TopicDetails
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
        val serializedData = objectMapper.writeValueAsString(mappings)
        redisTemplate.opsForValue().set("service-mappings", serializedData)
    }

    private fun getMappings(): Map<String, Any>? {
        val serializedData = redisTemplate.opsForValue().get("service-mappings")
        return serializedData?.let { objectMapper.readValue(it, object: TypeReference<Map<String, Any>>() {} ) }
    }

    fun getCachedSearchResultsOrNullList(searchRequest: SearchRequest, entityName: String): List<Pair<SearchParam, List<UUID>>?> {
        val result =  searchRequest.params.map { param ->
            val hashedKey = generateCacheKey(param)
            val fieldName = param.path.substringAfterLast(".")
            val cachedIds = try {
                redisTemplate.opsForValue().get(
                    "entities:$entityName:$fieldName:$hashedKey"
                )?.let {
                    objectMapper.readValue(it, object : TypeReference<List<UUID>>() {})
                }
            } catch (e: Exception) {
                log.error("Error retrieving cached value for key: $hashedKey", e)
                null
            }

            log.debug("Key: {}, Cached IDs: {}", hashedKey, cachedIds ?: "NOT FOUND")
            if (cachedIds != null) param to cachedIds else null
        }
        println("getCachedSearchResultsOrNullList: $result")
        return result
    }

    fun overwriteSearchResults(
        entityName: String,
        searchRequest: SearchRequest,
        resultIds: List<UUID>
    ) {
        val mappings = getMappings()?.toMutableMap() ?: mutableMapOf()

        val entityMap = mappings.getOrPut("entities") { mutableMapOf<String, Any>() } as MutableMap<String, Any>

        searchRequest.params.forEach { param ->
            val hashedKey = generateCacheKey(param)
            val fieldName = param.path.substringAfterLast(".")

            val fieldMap = (entityMap.getOrPut(entityName) { mutableMapOf<String, Any>() } as MutableMap<String, Any>)
                .getOrPut(fieldName) { mutableMapOf<String, List<UUID>>() } as MutableMap<String, List<UUID>>

            fieldMap[hashedKey] = resultIds
        }

        saveMappings(mappings)
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

}
