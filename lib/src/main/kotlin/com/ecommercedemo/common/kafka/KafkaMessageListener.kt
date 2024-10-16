package com.ecommercedemo.common.kafka

interface KafkaMessageListener<T> {
    fun onMessage(message: T)
}