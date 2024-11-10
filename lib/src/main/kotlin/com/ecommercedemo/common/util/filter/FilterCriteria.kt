package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.validation.comparison.ComparisonMethod

data class FilterCriteria<T>(
    val entitySimpleName: String,
    val comparison: ComparisonMethod? = null,
    val attribute: String,
    val value: Any? = null,
    val nestedFilters: List<FilterCriteria<T>> = emptyList()
)
