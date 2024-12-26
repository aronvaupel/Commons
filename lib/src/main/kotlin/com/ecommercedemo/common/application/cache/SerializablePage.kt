package com.ecommercedemo.common.application.cache

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.reflect.KClass


data class SerializablePage<T: KClass<out BaseEntity>> @JsonCreator constructor(
    @JsonProperty("content") val content: MutableList<T>,
    @JsonProperty("page")val page: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Long,
)