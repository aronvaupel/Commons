package com.ecommercedemo.common.application.cache

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class SerializablePage<T> @JsonCreator constructor(
    @JsonProperty("content") val content: MutableList<T>,
    @JsonProperty("page")val page: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Long,
)