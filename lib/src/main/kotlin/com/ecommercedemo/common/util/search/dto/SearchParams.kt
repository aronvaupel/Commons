package com.ecommercedemo.common.util.search.dto

import com.ecommercedemo.common.util.search.Operator
import com.ecommercedemo.common.validation.operator.ValidOperator

@ValidOperator
data class SearchParams(
    val operator: Operator,
    val searchValue: Any,
    val path: String // "username", "userInfo.address.city", "userInfo.pseudoProperties.occupation.company"
)
