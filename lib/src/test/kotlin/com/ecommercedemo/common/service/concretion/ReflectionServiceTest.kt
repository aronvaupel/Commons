package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.full.primaryConstructor

class ReflectionServiceTest {

    private lateinit var reflectionService: ReflectionService

    @BeforeEach
    fun setup() {
        reflectionService = ReflectionService()
    }

    data class TestEntity(
        var mutableField: String = "",
        val immutableField: String = ""
    ) : BaseEntity()

    @Test
    fun `test findConstructorWithArgs finds constructor with parameters`() {
        val constructor = reflectionService.findConstructorWithArgs(TestEntity::class)

        assertNotNull(constructor)
        assertEquals(2, constructor.parameters.size)
    }

    @Test
    fun `test findConstructorWithArgs throws exception when no constructor found`() {
        class NoArgsEntity : BaseEntity()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            reflectionService.findConstructorWithArgs(NoArgsEntity::class)
        }

        assertEquals("No suitable constructor found for NoArgsEntity", exception.message)
    }

    @Test
    fun `test getConstructorParams retrieves constructor parameters`() {
        val constructor = TestEntity::class.primaryConstructor!!
        val parameters = reflectionService.getConstructorParams(constructor)

        assertNotNull(parameters)
        assertEquals(2, parameters.size)
        assertTrue(parameters.any { it.name == "mutableField" })
        assertTrue(parameters.any { it.name == "immutableField" })
    }
}
