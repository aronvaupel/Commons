package com.ecommercedemo.common.redis.keys

import com.ecommercedemo.common.redis.values.TopicDetails
import com.fasterxml.jackson.annotation.JsonProperty

data class KafkaTopicRegistry(
    @JsonProperty("kafka-topic-registry") val topics: MutableMap<String, TopicDetails>
)