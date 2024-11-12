package com.ecommercedemo.common.util.search.dto

import jakarta.persistence.criteria.Path

data class ResolvedPathInfo(
    val jpaPath: Path<*>,
    val jsonSegments: List<String>
)