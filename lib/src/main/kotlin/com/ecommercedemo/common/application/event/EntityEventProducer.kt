package com.ecommercedemo.common.application.event

import com.ecommercedemo.common.application.cache.RedisService

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntityEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val redisService: RedisService
) {

    fun <T : Any> emit(
        entityClass: Class<T>,
        id: UUID,
        entityEventType: EntityEventType,
        properties: MutableMap<String, Any?>
    ) {
        val topic = entityClass.simpleName
        val kafkaRegistry = redisService.getKafkaRegistry()

        if (kafkaRegistry.topics.containsKey(topic)) {
            val event = EntityEvent(
                id = id,
                type = entityEventType,
                properties = properties
            )
            kafkaTemplate.send(topic, event)
            println("Produced event for topic: $topic, event: $event")
        } else {
            throw IllegalArgumentException("Topic $topic is not registered in Redis.")
        }
    }

}