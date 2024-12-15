package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.cache.RedisService
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Service

@Service
class DynamicTopicRegistration @Autowired constructor(
    private val redisService: RedisService,
    private val kafkaAdmin: KafkaAdmin
) {

    @Value("\${kafka.default.partitions:1}")
    private val defaultPartitions: Int = 1

    @Value("\${kafka.default.replication-factor:1}")
    private val defaultReplicationFactor: Int = 1

    fun declareKafkaTopics(upstreamEntityNames: List<String>) {
        redisService.registerAsTopics(upstreamEntityNames)
        upstreamEntityNames.forEach { topicName ->
            val topic: NewTopic = TopicBuilder.name(topicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicationFactor)
                .build()

            kafkaAdmin.createOrModifyTopics(topic)
        }
    }
}