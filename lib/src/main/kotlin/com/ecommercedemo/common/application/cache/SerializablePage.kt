package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
data class SerializablePage<T: BaseEntity> @JsonCreator constructor(
    @JsonProperty("content")
    val content: MutableList<T>,
    @JsonProperty("page")val page: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Long,
)