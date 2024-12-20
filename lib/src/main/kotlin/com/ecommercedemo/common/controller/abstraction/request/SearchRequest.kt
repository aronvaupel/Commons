package com.ecommercedemo.common.controller.abstraction.request

import com.ecommercedemo.common.controller.abstraction.util.SearchParam

data class SearchRequest(
    val params: List<SearchParam> = emptyList()
)
