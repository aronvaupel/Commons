package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.controller.abstraction.request.SaveRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.http.HttpStatus
import java.util.*

interface IRestService<T: BaseEntity> {
    fun update(id: UUID, request: SaveRequest): T
    fun getSingle(id: UUID): T
    fun getMultiple(ids: List<UUID>): List<T>
    fun search(request: SearchRequest): List<T>
    fun create(request: SaveRequest): T
    fun delete(id: UUID) : HttpStatus
}