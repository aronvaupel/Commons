package com.ecommercedemo.common.application.search.dto

import com.ecommercedemo.common.application.search.Operator

@com.ecommercedemo.common.application.validation.operator.ValidOperator
data class SearchParams(
    val operator: Operator,
    val searchValue: Any?,
    val path: String // "username", "userInfo.address.city", "userInfo.pseudoProperties.occupation.company"
)
