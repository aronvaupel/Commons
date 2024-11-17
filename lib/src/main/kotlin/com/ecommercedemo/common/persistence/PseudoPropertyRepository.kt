package com.ecommercedemo.common.persistence

import com.ecommercedemo.common.model.PseudoProperty
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PseudoPropertyRepository : JpaRepository<PseudoProperty, UUID> {
    @Modifying
    @Query(
        value = """
        INSERT INTO pseudo_properties (created_at, entity_simple_name, key, type_descriptor, updated_at, id)
        VALUES (:#{#pseudoProperty.createdAt}, :#{#pseudoProperty.entitySimpleName}, :#{#pseudoProperty.key}, CAST(:#{#pseudoProperty.typeDescriptor} AS JSONB), :#{#pseudoProperty.updatedAt}, :#{#pseudoProperty.id})
    """,
        nativeQuery = true
    )
    fun saveWithJsonb(@Param("pseudoProperty") pseudoProperty: PseudoProperty)

}