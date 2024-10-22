package com.ecommercedemo.common.kafka

import com.ecommercedemo.common.redis.RedisService
import com.ecommercedemo.common.util.springboot.EntityScanner
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManagerFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@ConditionalOnBean(EntityManagerFactory::class)
class DynamicTopicListener(
    private val redisService: RedisService,
    private val entityScanner: EntityScanner,
    private val kafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, Any>,
) {
    private val listenerContainers = mutableMapOf<String, MessageListenerContainer>()
    private lateinit var downstreamEntities: List<String>

    @PostConstruct
    fun init() {
        downstreamEntities = entityScanner.getDownstreamEntityNames()
        manageListeners()
    }

    @Scheduled(fixedRate = 30000)
    fun manageListeners() {
        val kafkaTopics = redisService.getKafkaRegistry()
        downstreamEntities.forEach { entity ->
            val topicDetails = kafkaTopics.topics[entity]
            if (topicDetails != null) {
                if (!listenerContainers.containsKey(entity)) {
                    createKafkaListener(entity)
                    redisService.registerConsumer(entity)
                }
            } else {
               if (listenerContainers.contains(entity)) {
                   stopKafkaListener(entity)
               }
            }
        }
    }


    private fun createKafkaListener(topic: String) {
        val listenerContainer = kafkaListenerContainerFactory.createContainer(topic)
        listenerContainer.setupMessageListener { message: ConsumerRecord<String, Any> ->
            println("Received message from topic $topic: ${message.value()}")
        }
        listenerContainer.start()
        listenerContainers[topic] = listenerContainer
        println("Kafka listener started for topic: $topic")
    }

    private fun stopKafkaListener(topic: String) {
        listenerContainers[topic]?.stop()
        listenerContainers.remove(topic)
        println("Kafka listener stopped for topic: $topic")
    }
}