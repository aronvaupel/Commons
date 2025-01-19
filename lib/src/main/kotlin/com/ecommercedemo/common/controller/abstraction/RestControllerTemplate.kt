package com.ecommercedemo.common.controller.abstraction

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Tag(name = "Generic CRUD Operations", description = "Base CRUD operations for all entities.")
@Suppress("unused")
abstract class RestControllerTemplate<T : BaseEntity> {

    @Autowired
    private lateinit var service: RestServiceTemplate<T>

    @Operation(summary = "Create a new entity.")
    @PostMapping
    open fun create(
        @RequestBody request: CreateRequest
    ): ResponseEntity<T> {
          return ResponseEntity.ok(service.create(request))
    }

    @Operation(summary = "Update an existing entity.")
    @PatchMapping
    open fun update(
        @RequestBody request: UpdateRequest
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.update(request))
    }

    @Operation(summary = "Delete an entity by ID.")
    @DeleteMapping("/{id}")
    open fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<HttpStatus> {
        return ResponseEntity.ok(service.delete(id))
    }

    @Operation(summary = "Retrieve an entity by ID.")
    @GetMapping("/{id}")
    open fun getSingle(
        @PathVariable id: UUID
    ): ResponseEntity<T> {
        return ResponseEntity.ok(service.getSingle(id))
    }

    @Operation(summary = "Retrieve multiple entities by ID.")
    @GetMapping
    open fun getMultiple(
        @RequestParam ids: List<UUID>,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.getMultiple(ids, page, size))
    }

    @Operation(summary = "Retrieve all entities (paged).")
    @GetMapping("/all")
    open fun getAllPaged(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.getAllPaged(page, size))
    }

    @Operation(summary = "Search/filter for entities.")
    @GetMapping("/search")
    open fun search(
        @RequestBody request: SearchRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.search(request, page, size))
    }
}
