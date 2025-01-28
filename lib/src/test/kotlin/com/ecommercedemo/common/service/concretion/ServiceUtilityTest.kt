package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ServiceUtilityTest {

    private val mockPseudoPropertyRepository = mock(_PseudoPropertyRepository::class.java)
    private val mockReflectionService = mock(ReflectionService::class.java)
    private val serviceUtility = ServiceUtility<TestEntity>(mockPseudoPropertyRepository, mockReflectionService)

    data class TestEntity(
        var name: String,
        var age: Int,
        var nestedEntity: NestedEntity? = null
    ) : AugmentableBaseEntity()

    data class NestedEntity(var description: String) : AugmentableBaseEntity()

    @Test
    fun `updateExistingEntity should update fields correctly`() {
        val entity = TestEntity(name = "Old Name", age = 25)
        val data = mapOf("name" to "New Name", "age" to 30)
        `when`(mockReflectionService.findMutableProperties(entity)).thenReturn(
            mapOf(
                "name" to TestEntity::name,
                "age" to TestEntity::age
            )
        )

        val updatedEntity = serviceUtility.updateExistingEntity(data, entity)

        assertEquals("New Name", updatedEntity.name)
        assertEquals(30, updatedEntity.age)
    }

    @Test
    fun `updateExistingEntity should update nested entity`() {
        val nestedEntity = NestedEntity(description = "Old Description")
        val entity = TestEntity(name = "Name", age = 30, nestedEntity = nestedEntity)
        val data = mapOf("nestedEntity" to mapOf("description" to "New Description"))
        `when`(mockReflectionService.findMutableProperties(entity)).thenReturn(
            mapOf(
                "nestedEntity" to TestEntity::nestedEntity
            )
        )
        `when`(mockReflectionService.findMutableProperties(nestedEntity)).thenReturn(
            mapOf(
                "description" to NestedEntity::description
            )
        )

        val updatedEntity = serviceUtility.updateExistingEntity(data, entity)

        assertNotNull(updatedEntity.nestedEntity)
        assertEquals("New Description", updatedEntity.nestedEntity!!.description)
    }

    @Test
    fun `_PseudoProperty should be instantiated with valid values`() {
        val pseudoProperty = _PseudoProperty(
            entitySimpleName = "TestEntity",
            key = "prop1",
            typeDescriptor = TypeDescriptor.PrimitiveDescriptor(
                type = ValueType.STRING,
                isNullable = false
            )
        )

        assertEquals("TestEntity", pseudoProperty.entitySimpleName)
        assertEquals("prop1", pseudoProperty.key)
        assertEquals(ValueType.STRING, (pseudoProperty.typeDescriptor as TypeDescriptor.PrimitiveDescriptor).type)
        assertFalse((pseudoProperty.typeDescriptor as TypeDescriptor.PrimitiveDescriptor).isNullable)
    }
}
