package com.ecommercedemo.common.controller

import com.ecommercedemo.common.application.search.dto.SearchRequest
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import com.ecommercedemo.common.service.PseudoPropertyService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/pseudo-properties")
class PseudoPropertyController(
    private val pseudoPropertyService: PseudoPropertyService,
) {
    @GetMapping
    fun getPseudoProperties(
        @RequestBody request: SearchRequest
    ) : ResponseEntity<List<PseudoProperty>> {
        return ResponseEntity.ok(pseudoPropertyService.getPseudoProperties(request))
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
    fun renamePseudoProperty(
        @PathVariable id: UUID,
        @RequestParam newKey: String,
    ): ResponseEntity<PseudoProperty> {
        return ResponseEntity.ok(pseudoPropertyService.renamePseudoProperty(id, newKey))
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