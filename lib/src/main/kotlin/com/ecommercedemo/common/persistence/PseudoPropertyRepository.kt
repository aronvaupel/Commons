package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.util.search.dto.SearchRequest
import org.springframework.data.jpa.repository.JpaRepository

import java.util.*

interface PseudoPropertyRepository: JpaRepository<PseudoProperty, UUID> {
    fun save(pseudoProperty: PseudoProperty): PseudoProperty
    fun getAll(request: SearchRequest): List<PseudoProperty>
    fun delete(id: UUID)
}