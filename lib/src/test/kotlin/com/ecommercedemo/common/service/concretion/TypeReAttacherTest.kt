package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.springboot.SpringContextProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.metamodel.EntityType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationContext
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

open class BaseEntity(val id: UUID)

class TypeReAttacherTest {

    private val mockEntityManagerFactory = mock(EntityManagerFactory::class.java)
    private val mockEntityManager = mock(EntityManager::class.java)
    private val mockMetamodel = mock(jakarta.persistence.metamodel.Metamodel::class.java)
    private val mockApplicationContext = mock(ApplicationContext::class.java)
    private val objectMapper = jacksonObjectMapper()

    init {
        SpringContextProvider.applicationContext = mockApplicationContext

        `when`(mockApplicationContext.getBean(ObjectMapper::class.java)).thenReturn(objectMapper)
        `when`(mockEntityManagerFactory.createEntityManager()).thenReturn(mockEntityManager)
        `when`(mockEntityManager.getMetamodel()).thenReturn(mockMetamodel)
    }

    private val typeReAttacher = TypeReAttacher(mockEntityManagerFactory)

    data class TestEntity(val name: String, val age: Int) : BaseEntity(UUID.randomUUID())

    @Test
    fun `extractFieldTypesMap should return correct field types`() {
        val result = typeReAttacher.extractFieldTypesMap(TestEntity::class)
        assertEquals(
            mapOf(
                "id" to UUID::class.createType(),
                "name" to String::class.createType(),
                "age" to Int::class.createType()
            ),
            result
        )
    }

    @Test
    fun `reAttachType should map data correctly to entity fields`() {
        val inputData = mapOf("name" to "John", "age" to 30)
        mockEntityResolution(TestEntity::class)

        val result = typeReAttacher.reAttachType(inputData, "TestEntity")

        assertEquals(inputData, result)
    }

    @Test
    fun `resolveEntityClass should return correct entity class`() {
        mockEntityResolution(TestEntity::class)

        val result = typeReAttacher.resolveEntityClass("TestEntity")

        assertEquals(TestEntity::class, result)
    }

    @Test
    fun `resolveEntityClass should throw exception for unknown class`() {
        `when`(mockMetamodel.entities).thenReturn(emptySet())

        val exception = assertThrows<IllegalArgumentException> {
            typeReAttacher.resolveEntityClass("UnknownEntity")
        }
        assertTrue(exception.message!!.contains("Entity class not found for name"))
    }

    private fun mockEntityResolution(vararg entities: KClass<*>) {
        val entityTypes = entities.map { entity ->
            val mockEntityType = mock(EntityType::class.java) as EntityType<*>
            `when`(mockEntityType.javaType).thenReturn(entity.java)
            mockEntityType
        }.toSet()

        `when`(mockMetamodel.entities).thenReturn(entityTypes)
    }
}

