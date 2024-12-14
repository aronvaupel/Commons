package com.ecommercedemo.common.controller.abstraction.util

@com.ecommercedemo.common.application.validation.operator.ValidOperator
data class SearchParams(
    val operator: Operator,
    val searchValue: Any? = null,
    val path: String // "username", "userInfo.address.city", "userInfo.pseudoProperties.occupation.company"
)
