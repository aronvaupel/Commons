package com.ecommercedemo.common.application.cache

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Pageable

data class SerializablePage<T> @JsonCreator constructor(
    @JsonProperty("content") val content: MutableList<T>,
    @JsonProperty("pageNumber")val pageable: Pageable,
    @JsonProperty("pageSize") val totalElements: Long,
)