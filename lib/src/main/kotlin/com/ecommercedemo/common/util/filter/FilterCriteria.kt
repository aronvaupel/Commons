package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.validation.comparison.ComparisonMethod
import kotlin.reflect.KProperty1

data class FilterCriteria<T>(
    val comparison: ComparisonMethod,
    val attribute: KProperty1<T, *>,
    val value: Any? = null,
    val nestedFilters: List<FilterCriteria<T>> = emptyList()
)
