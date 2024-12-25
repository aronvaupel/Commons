package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EntityRepository<T: BaseEntity, ID> : JpaRepository<T, ID> {
    fun findAllById(ids: List<UUID>, pageable: Pageable): Page<T>
}