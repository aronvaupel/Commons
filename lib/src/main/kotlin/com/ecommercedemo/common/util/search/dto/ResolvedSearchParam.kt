package com.ecommercedemo.common.util.search.dto

import jakarta.persistence.criteria.Path

data class ResolvedSearchParam(
    val deserializedValue: Any?,
    val jpaPath: Path<*>,
    val jsonSegments: List<String>
)