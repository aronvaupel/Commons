package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.operator.ValidOperator

@ValidOperator
data class SearchParam(
    val operator: Operator,
    val searchValue: Any? = null,
    val path: String // "username", "userInfo.address.city", "userInfo.pseudoProperties.occupation.company"
)
