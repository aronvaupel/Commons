package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.data.domain.Page
import java.util.*

interface IDownstreamRestService<T: BaseEntity> {
    fun getSingle(id: UUID): T
    fun getMultiple(ids: List<UUID>, page: Int, size: Int): Page<T>
    fun getAllPaged(page: Int, size: Int): Page<T>
    fun search(request: SearchRequest, page: Int, size: Int): Page<T>
}