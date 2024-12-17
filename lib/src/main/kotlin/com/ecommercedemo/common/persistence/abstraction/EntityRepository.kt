package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface EntityRepository<T: BaseEntity, ID> : JpaRepository<T, ID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    fun findByIdForUpdate(id: ID): T

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids")
    fun findAllByIdForUpdate(ids: List<ID>): List<T>
}