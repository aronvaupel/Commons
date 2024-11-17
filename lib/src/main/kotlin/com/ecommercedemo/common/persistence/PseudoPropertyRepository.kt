package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface PseudoPropertyRepository: JpaRepository<PseudoProperty, UUID> {
    @Modifying
    @Query(
        value = "INSERT INTO pseudo_properties (created_at, entity_simple_name, key, type_descriptor, updated_at, id) " +
                "VALUES (:createdAt, :entitySimpleName, :key, CAST(:typeDescriptor AS JSONB), :updatedAt, :id)",
        nativeQuery = true
    )
    fun save(
        @Param("createdAt") createdAt: LocalDateTime,
        @Param("entitySimpleName") entitySimpleName: String,
        @Param("key") key: String,
        @Param("typeDescriptor") typeDescriptor: String,
        @Param("updatedAt") updatedAt: LocalDateTime,
        @Param("id") id: UUID
    )
}