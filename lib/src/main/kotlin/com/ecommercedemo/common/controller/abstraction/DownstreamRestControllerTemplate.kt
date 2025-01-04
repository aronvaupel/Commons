package com.ecommercedemo.common.controller.abstraction

import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.*


@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
abstract class DownstreamRestControllerTemplate<T : BaseEntity> {

    @Autowired
    private lateinit var service: DownstreamRestServiceTemplate<T>

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
