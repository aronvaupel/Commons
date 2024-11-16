package com.ecommercedemo.common.application.cache.values

data class TopicDetails(
    var producer: Microservice,
    val consumers: MutableSet<Microservice>
)