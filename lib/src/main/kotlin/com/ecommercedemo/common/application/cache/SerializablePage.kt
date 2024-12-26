package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class SerializablePage<T: BaseEntity> @JsonCreator constructor(
    @JsonProperty("content")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    val content: MutableList<T>,
    @JsonProperty("page")val page: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Long,
)