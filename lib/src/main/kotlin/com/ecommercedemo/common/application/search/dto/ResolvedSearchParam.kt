package com.ecommercedemo.common.application.search.dto

import jakarta.persistence.criteria.Path

data class ResolvedSearchParam(
    val deserializedValue: Any?,
    val jpaPath: Path<*>,
    val jsonSegments: List<String>
)