package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EntityRepository<T: BaseEntity, ID> : JpaRepository<T, ID> {
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id", nativeQuery = true)
    fun findByIdForUpdate(id: ID): T

    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids", nativeQuery = true)
    fun findAllByIdForUpdate(ids: List<ID>): List<T>
}