package com.ecommercedemo.common.application.event

import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
open class KafkaLocalConfig : KafkaConfig() {
    override fun kafkaProperties(): Map<String, Any> {
        return super.kafkaProperties().plus(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092")
    }
}