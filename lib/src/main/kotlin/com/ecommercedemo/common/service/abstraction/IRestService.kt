package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import java.util.*

interface IRestService<T: BaseEntity> {
    fun update(request: UpdateRequest): T?
    fun getSingle(id: UUID): T
    fun getMultiple(ids: List<UUID>, page: Int, size: Int): Page<T>
    fun getAllPaged(page: Int, size: Int): Page<T>
    fun search(request: SearchRequest, page: Int, size: Int): Page<T>
    fun create(request: CreateRequest): T?
    fun delete(id: UUID) : HttpStatus
}