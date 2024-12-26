package com.ecommercedemo.common.application.cache

import org.springframework.data.domain.Pageable

data class SerializablePage<T>(
    val content: MutableList<T>,
    val pageable: Pageable,
    val totalElements: Long,
)