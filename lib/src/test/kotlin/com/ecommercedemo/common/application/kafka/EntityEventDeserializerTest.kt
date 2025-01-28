package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

class EntityEventDeserializerTest {

    private lateinit var objectMapper: ObjectMapper
    private lateinit var typeReAttacher: TypeReAttacher
    private lateinit var deserializer: EntityEventDeserializer

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        typeReAttacher = mock(TypeReAttacher::class.java)
        deserializer = EntityEventDeserializer(objectMapper, typeReAttacher)

        val module = SimpleModule()
        module.addDeserializer(EntityEvent::class.java, deserializer)
        objectMapper.registerModule(module)
    }

    @Test
    fun `should deserialize valid EntityEvent`() {
        val entityClassName = "TestEntity"
        val id = UUID.randomUUID()
        val modificationType = ModificationType.CREATE
        val properties = mapOf("key1" to "value1", "key2" to 42)

        val json = """
            {
                "entityClassName": "$entityClassName",
                "id": "$id",
                "type": "${modificationType.name}",
                "properties": {
                    "key1": "value1",
                    "key2": 42
                }
            }
        """
        `when`(typeReAttacher.reAttachType(properties, "_TestEntity")).thenReturn(properties)

        val result = objectMapper.readValue(json, EntityEvent::class.java)

        assertEquals(entityClassName, result.entityClassName)
        assertEquals(id, result.id)
        assertEquals(modificationType, result.type)
        assertEquals(properties, result.properties)
    }

    @Test
    fun `should handle PermissionUserAssociation class name normalization`() {
        val entityClassName = "PermissionUserAssociation"
        val id = UUID.randomUUID()
        val modificationType = ModificationType.UPDATE
        val properties = mapOf("field1" to "value")

        val json = """
            {
                "entityClassName": "$entityClassName",
                "id": "$id",
                "type": "${modificationType.name}",
                "properties": {
                    "field1": "value"
                }
            }
        """

        `when`(typeReAttacher.reAttachType(properties, "PermissionUserAssociation")).thenReturn(properties)

        val result = objectMapper.readValue(json, EntityEvent::class.java)

        assertEquals(entityClassName, result.entityClassName)
        assertEquals(id, result.id)
        assertEquals(modificationType, result.type)
        assertEquals(properties, result.properties)
    }

    @Test
    fun `should throw exception for missing entityClassName`() {
        val json = """
            {
                "id": "${UUID.randomUUID()}",
                "type": "CREATE",
                "properties": {}
            }
        """

        val exception = assertThrows<IllegalArgumentException> {
            objectMapper.readValue(json, EntityEvent::class.java)
        }

        assertEquals("Missing or invalid 'entityClassName'", exception.message)
    }

    @Test
    fun `should throw exception for missing id`() {
        val json = """
            {
                "entityClassName": "TestEntity",
                "type": "CREATE",
                "properties": {}
            }
        """

        val exception = assertThrows<IllegalArgumentException> {
            objectMapper.readValue(json, EntityEvent::class.java)
        }

        assertEquals("Missing or invalid 'id'", exception.message)
    }

    @Test
    fun `should throw exception for missing type`() {
        val json = """
            {
                "entityClassName": "TestEntity",
                "id": "${UUID.randomUUID()}",
                "properties": {}
            }
        """

        val exception = assertThrows<IllegalArgumentException> {
            objectMapper.readValue(json, EntityEvent::class.java)
        }

        assertEquals("Missing or invalid 'type'", exception.message)
    }

    @Test
    fun `should throw exception for missing properties`() {
        val json = """
            {
                "entityClassName": "TestEntity",
                "id": "${UUID.randomUUID()}",
                "type": "CREATE"
            }
        """

        val exception = assertThrows<IllegalArgumentException> {
            objectMapper.readValue(json, EntityEvent::class.java)
        }

        assertEquals("Missing 'properties'", exception.message)
    }

    @Test
    fun `should throw exception for invalid properties type`() {
        val json = """
            {
                "entityClassName": "TestEntity",
                "id": "${UUID.randomUUID()}",
                "type": "CREATE",
                "properties": "not_a_json_object"
            }
        """

        val exception = assertThrows<IllegalArgumentException> {
            objectMapper.readValue(json, EntityEvent::class.java)
        }

        assertEquals("Properties' must be a JSON object", exception.message)
    }

}
