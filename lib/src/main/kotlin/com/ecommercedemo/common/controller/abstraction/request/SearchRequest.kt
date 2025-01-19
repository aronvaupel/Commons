package com.ecommercedemo.common.controller.abstraction.request

import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to search for entities.")
data class SearchRequest(
    val params: List<SearchParam> = emptyList()
)
