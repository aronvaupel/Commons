package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.criteria.Root
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class PathResolverTest {
    private val validator: SearchParamValidation = mock(SearchParamValidation::class.java)
    private val converter: SearchParamConverter = mock(SearchParamConverter::class.java)
    private val pseudoPropertyRepository: _PseudoPropertyRepository = mock(_PseudoPropertyRepository::class.java)
    private val objectMapper: ObjectMapper = mock(ObjectMapper::class.java)

    private val pathResolver = PathResolver(
        validator,
        converter,
        pseudoPropertyRepository,
        objectMapper
    )

    @Test
    fun `resolvePath throws exception for invalid path segment`() {
        val params = SearchParam(Operator.EQUALS, "value", "invalidProperty")
        val root = mock(Root::class.java) as Root<MyEntity>
        `when`(root.javaType).thenReturn(MyEntity::class.java)

        doThrow(IllegalArgumentException("Invalid property")).`when`(validator)
            .validateFieldExistsAndIsAccessible("invalidProperty", MyEntity::class.java)

        val exception = assertThrows<IllegalArgumentException> {
            pathResolver.resolvePath(params, root)
        }

        assertEquals("Invalid property", exception.message)
    }
}

data class MyEntity(override var id: UUID = UUID.randomUUID(), val name: String) : BaseEntity()
