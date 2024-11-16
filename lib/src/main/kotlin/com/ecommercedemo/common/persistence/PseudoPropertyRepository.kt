package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PseudoPropertyRepository: JpaRepository<PseudoProperty, UUID> {
    fun save(pseudoProperty: PseudoPropertyDto): PseudoProperty
}