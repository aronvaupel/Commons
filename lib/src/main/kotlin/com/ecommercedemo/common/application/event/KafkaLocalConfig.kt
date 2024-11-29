package com.ecommercedemo.common.application.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
open class KafkaLocalConfig(
    objectMapper: ObjectMapper
) : KafkaConfig(objectMapper) {


    override fun producerProperties(): Map<String, Any> {
        return super.producerProperties().plus(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092")
    }

    override fun consumerProperties(): Map<String, Any> {
        return super.consumerProperties().plus(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092")
    }
}