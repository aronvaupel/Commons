package com.ecommercedemo.common.kafka

import com.ecommercedemo.common.redis.RedisService
import com.ecommercedemo.common.util.springboot.EntityScanner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class DynamicTopicListener(
    @Autowired(required = false)
    @Lazy
    private val eventHandler: IEventHandler<EntityEvent>?,
    private val redisService: RedisService,
    private val entityScanner: EntityScanner
) {

    // Listen to Kafka topics and validate topics before processing events
    @KafkaListener(topics = ["#{@dynamicTopicListener.dynamicTopics()}"], groupId = "\${kafka.group-id}")
    fun listen(event: EntityEvent) {
        println("Received event from Kafka: $event")
        eventHandler?.handle(event)  ?: println("No handler available for this event.")
    }

    // Provide dynamic topics to the listener
    @Bean
    fun dynamicTopics(): List<String> {
        waitForTopicRegistrationCompletion()  // Wait for all services to finish topic registration
        val relevantTopics = entityScanner.getDownstreamEntityNames()
        validateTopics(relevantTopics)  // Validate topics before proceeding
        if (relevantTopics.isEmpty()) {
            println("No need to listen for any topics.")
        } else {
            println("Listening for topics: $relevantTopics")
        }
        return relevantTopics
    }

    // Wait for Redis to confirm that topic registration is complete
    private fun waitForTopicRegistrationCompletion() {
        while (!redisService.isRegistrationComplete()) {
            println("Waiting for Kafka topic name registration to complete...")
            Thread.sleep(1000)
        }
        println("Kafka topic name registration is complete.")
    }

    // Validate that the required topics exist in Redis
    private fun validateTopics(topics: List<String>) {
        val registeredTopics = redisService.getKafkaTopicNames()
        topics.forEach { topic ->
            if (!registeredTopics.contains(topic)) {
                throw IllegalStateException("Expected topic $topic is not registered in Kafka.")
            }
        }
        println("All required topics are registered in Kafka.")
    }
}