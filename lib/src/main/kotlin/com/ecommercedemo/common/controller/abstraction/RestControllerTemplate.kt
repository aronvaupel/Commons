package com.ecommercedemo.common.controller.abstraction

import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
abstract class RestControllerTemplate<T : BaseEntity> {

    @Autowired
    private lateinit var service: RestServiceTemplate<T>

    @PostMapping
    open fun create(
        @RequestBody request: CreateRequest
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
        @RequestParam ids: List<UUID>,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.getMultiple(ids, page, size))
    }

    @GetMapping("/all")
    open fun getAllPaged(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.getAllPaged(page, size))
    }

    @GetMapping("/search")
    open fun search(
        @RequestBody request: SearchRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "1000") size: Int,
    ): ResponseEntity<Page<T>> {
        return ResponseEntity.ok(service.search(request, page, size))
    }
}
