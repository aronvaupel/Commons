package com.ecommercedemo.common.kafka

import com.ecommercedemo.common.redis.RedisService
import jakarta.persistence.EntityManagerFactory
import org.hibernate.validator.constraints.UUID
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
@ConditionalOnBean(EntityManagerFactory::class)
class EntityEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val redisService: RedisService
) {

    fun <T : Any> emit(
        entityClass: Class<T>,
        id: UUID,
        entityEventType: EntityEventType,
        properties: List<ChangedProperty>? = null
    ) {
        val topic = entityClass.simpleName
        val topics = redisService.getKafkaTopicNames()

        if (topics.contains(topic)) {
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