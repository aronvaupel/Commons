package com.ecommercedemo.common.redis.values

import com.fasterxml.jackson.annotation.JsonProperty

data class KafkaRegistry(
    @JsonProperty("kafka-topics") val topics: MutableMap<String, TopicDetails>
)