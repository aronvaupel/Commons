package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.validation.modification.ModificationType
import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntityEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val redisService: RedisService,
) {
    private val log = KotlinLogging.logger {}

    fun emit(
        entityClassName: String,
        id: UUID,
        modificationType: ModificationType,
        properties: MutableMap<String, Any?>
    ) {
        val kafkaRegistry = redisService.getKafkaRegistry()
        val topic = entityClassName.replaceFirstChar { it.lowercaseChar() }
        if (kafkaRegistry.topics.containsKey(entityClassName)) {
            val event = EntityEvent(
                entityClassName = entityClassName,
                id = id,
                type = modificationType,
                properties = properties
            )

            kafkaTemplate.send(topic, event)
            log.info("Produced event for topic: $entityClassName, event: $event")
        } else {
            throw IllegalArgumentException("Topic $entityClassName is not registered in Redis.")
        }
    }

}