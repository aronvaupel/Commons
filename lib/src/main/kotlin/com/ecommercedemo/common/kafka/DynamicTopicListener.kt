package com.ecommercedemo.common.kafka

import com.ecommercedemo.common.redis.RedisService
import com.ecommercedemo.common.util.springboot.EntityScanner
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.stereotype.Service

@Service
class DynamicTopicListener(
    @Autowired(required = false) @Lazy
    private val eventHandler: IEventHandler<EntityEvent>?,  // Optional event handler
    private val redisService: RedisService,
    private val entityScanner: EntityScanner,
    private val kafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, EntityEvent>
) {

    @PostConstruct
    fun init() {
        val topics = resolveDynamicTopics()
        if (topics.isNotEmpty()) {
            registerKafkaListeners(topics)
        }
    }

    // Dynamically register Kafka listeners for the resolved topics
    private fun registerKafkaListeners(topics: List<String>) {
        topics.forEach { topic ->
            val listenerContainer = kafkaListenerContainerFactory.createContainer(topic)
            listenerContainer.setupMessageListener { message: ConsumerRecord<String, EntityEvent> ->
                println("Received event from Kafka: ${message.value()}")
                eventHandler?.handle(message.value()) ?: println("No handler available for this event.")
            }
            listenerContainer.start()  // Start the container directly without using KafkaListenerEndpointRegistry
            println("Registered Kafka listener for topic: $topic")
        }
    }

    // Dynamically resolve topics
    private fun resolveDynamicTopics(): List<String> {
        waitForTopicRegistrationCompletion()
        val relevantTopics = entityScanner.getDownstreamEntityNames()
        validateTopics(relevantTopics)
        return relevantTopics
    }

    private fun waitForTopicRegistrationCompletion() {
        while (!redisService.isRegistrationComplete()) {
            println("Waiting for Kafka topic registration to complete...")
            Thread.sleep(1000)
        }
        println("Kafka topic registration is complete.")
    }

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