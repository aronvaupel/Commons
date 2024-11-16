package com.ecommercedemo.common.controller

import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import com.ecommercedemo.common.service.PseudoPropertyService
import com.ecommercedemo.common.util.search.dto.SearchRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/pseudo-properties")
class PseudoPropertyController(
    private val pseudoPropertyService: PseudoPropertyService,
) {
    @GetMapping
    fun getAll(
        @RequestBody request: SearchRequest
    ) : ResponseEntity<List<PseudoProperty>> {
        return ResponseEntity.ok(pseudoPropertyService.getAll(request))
    }

    @PostMapping
    fun createPseudoProperty(
        @RequestBody body: PseudoPropertyDto
    ): ResponseEntity<PseudoProperty> {
        return ResponseEntity.ok(
            pseudoPropertyService.addPseudoProperty(body)
        )
    }

    @PutMapping("/{id}")
    fun updatePseudoProperty(
        @PathVariable id: UUID,
        @RequestBody body: PseudoPropertyDto
    ): ResponseEntity<PseudoProperty> {
        return ResponseEntity.ok(pseudoPropertyService.update(id, body))
    }

    @DeleteMapping("/{id}")
    fun deletePseudoProperty(
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        pseudoPropertyService.deletePseudoProperty(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    fun getPseudoProperty(
        @PathVariable id: UUID
    ): ResponseEntity<PseudoProperty> {
        return ResponseEntity.ok(pseudoPropertyService.getById(id))
    }

}