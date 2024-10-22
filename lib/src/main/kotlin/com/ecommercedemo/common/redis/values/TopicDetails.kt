package com.ecommercedemo.common.redis.values

data class TopicDetails(
    var producer: Microservice,
    val consumers: MutableSet<Microservice>
)