package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.http.HttpStatus
import java.util.*

interface IService<T: BaseEntity> {
    fun update(request: UpdateRequest): T
    fun getSingle(id: UUID): T
    fun getMultiple(ids: List<UUID>): List<T>
    fun search(request: SearchRequest): List<T>
    fun create(request: CreateRequest<T>): T
    fun delete(id: UUID) : HttpStatus
    fun createByEvent(event: EntityEvent<T>)
    fun updateByEvent(event: EntityEvent<T>)
    fun deleteByEvent(event: EntityEvent<T>)
}