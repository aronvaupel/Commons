package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.cache.RedisService
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntityEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val redisService: RedisService,
) {

    fun emit(
        entityClassName: String,
        id: UUID,
        entityEventType: EntityEventType,
        properties: MutableMap<String, Any?>
    ) {
        val kafkaRegistry = redisService.getKafkaRegistry()

        if (kafkaRegistry.topics.containsKey(entityClassName)) {
            val event = EntityEvent(
                entityClassName = entityClassName,
                id = id,
                type = entityEventType,
                properties = properties
            )

            kafkaTemplate.send(entityClassName, event)
        } else {
            throw IllegalArgumentException("Topic $entityClassName is not registered in Redis.")
        }
    }

}