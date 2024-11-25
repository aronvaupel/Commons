package com.ecommercedemo.common.controller.abstraction

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.abstraction.ServiceTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

abstract class RestControllerTemplate<T : BaseEntity>(
    private val service: ServiceTemplate<T>
) {
    init {
        println("RestControllerTemplate initialized with service: $service")
    }

    @PostMapping
    fun create(
        @RequestBody request: CreateRequest<T>
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.create(request))
    }

    @PatchMapping
    fun update(
        @RequestBody request: UpdateRequest
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.update(request))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<HttpStatus> {
        return ResponseEntity.ok(service.delete(id))
    }

    @GetMapping("/{id}")
    fun getSingle(
        @PathVariable id: UUID
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.getSingle(id))
    }

    @GetMapping
    fun getMultiple(
        @RequestParam ids: List<UUID>
    ): ResponseEntity<List<T>> {
        return ResponseEntity.ok(service.getMultiple(ids))
    }

    @GetMapping("/search")
    fun search(
        @RequestBody request: SearchRequest
    ): ResponseEntity<List<T>> {
        return ResponseEntity.ok(service.search(request))
    }
}
