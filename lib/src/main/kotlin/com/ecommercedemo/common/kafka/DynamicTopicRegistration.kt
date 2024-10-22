package com.ecommercedemo.common.kafka

import com.ecommercedemo.common.redis.RedisService
import jakarta.persistence.EntityManagerFactory
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Service

@Service
@ConditionalOnBean(EntityManagerFactory::class)
class DynamicTopicRegistration(
    private val redisService: RedisService,
    private val kafkaAdmin: KafkaAdmin
) {

    @Value("\${kafka.default.partitions:1}")
    private val defaultPartitions: Int = 1

    @Value("\${kafka.default.replication-factor:1}")
    private val defaultReplicationFactor: Int = 1

    fun declareKafkaTopics(upstreamEntityNames: List<String>) {
        redisService.registerAsTopics(upstreamEntityNames)
        println("Attempting to declare Kafka topics for upstream entities: $upstreamEntityNames")
        upstreamEntityNames.forEach { topicName ->
            val topic: NewTopic = TopicBuilder.name(topicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicationFactor)
                .build()

            kafkaAdmin.createOrModifyTopics(topic)
            println("Declared Kafka topic: $topicName with $defaultPartitions partitions and $defaultReplicationFactor replication factor.")
        }
    }
}