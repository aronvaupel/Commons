package com.ecommercedemo.common.controller.abstraction.request

import com.ecommercedemo.common.controller.abstraction.util.SearchParams

data class SearchRequest(
    val params: List<SearchParams> = emptyList()
)
