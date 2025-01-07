package com.ecommercedemo.common.application.cache.keys

import com.ecommercedemo.common.application.cache.values.TopicDetails
import com.fasterxml.jackson.annotation.JsonProperty

data class KafkaTopicRegistry(
    @JsonProperty("kafka-topic-registry") val topics: MutableMap<String, TopicDetails?> = mutableMapOf(),
)