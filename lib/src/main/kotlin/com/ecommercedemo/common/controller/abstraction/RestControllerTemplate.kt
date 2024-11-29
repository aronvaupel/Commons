package com.ecommercedemo.common.controller.abstraction

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import jakarta.annotation.PostConstruct
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

abstract class RestControllerTemplate<T : BaseEntity>(
    private val service: RestServiceTemplate<T>
) {
    init {
        println("RestControllerTemplate initialized with service: $service")
    }

    @PostConstruct
    fun afterInit() {
        println("RestControllerTemplate post-construction with service: $service")
    }

    @PostMapping
    open fun create(
        @RequestBody request: CreateRequest<T>
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.create(request))
    }

    @PatchMapping
    open fun update(
        @RequestBody request: UpdateRequest
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.update(request))
    }

    @DeleteMapping("/{id}")
    open fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<HttpStatus> {
        return ResponseEntity.ok(service.delete(id))
    }

    @GetMapping("/{id}")
    open fun getSingle(
        @PathVariable id: UUID
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.getSingle(id))
    }

    @GetMapping
    open fun getMultiple(
        @RequestParam ids: List<UUID>
    ): ResponseEntity<List<T>> {
        return ResponseEntity.ok(service.getMultiple(ids))
    }

    @GetMapping("/search")
    open fun search(
        @RequestBody request: SearchRequest
    ): ResponseEntity<List<T>> {
        return ResponseEntity.ok(service.search(request))
    }
}
