package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.validation.comparison.ComparisonMethod

data class FilterCriteria(
    val entitySimpleName: String,
    val comparison: ComparisonMethod? = null,
    val jpaAttribute: String,
    val pseudoPropertyPathToKey: String? = null, // i.e. "gender" or "address.city" or "address.city.foundingDate" or "address.city.population"
    val value: Any? = null,
    val nestedFilter: FilterCriteria? = null,
)
