package com.ecommercedemo.common.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumerService {

    @KafkaListener(topics = ["your_topic"], groupId = "group_id")
    fun listen(message: String) {
        // Handle the message
    }
}