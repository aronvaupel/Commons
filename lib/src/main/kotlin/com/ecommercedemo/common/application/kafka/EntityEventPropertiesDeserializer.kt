package com.ecommercedemo.common.application.kafka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityManager
import java.util.*


class EntityEventPropertiesDeserializer(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper
) : JsonDeserializer<EntityEvent>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): EntityEvent {
        // Step 1: Parse JSON payload
        val rootNode = parser.codec.readTree<ObjectNode>(parser)

        // Step 2: Validate required fields
        val entityClassName = rootNode["entityClassName"]?.asText()
            ?: throw IllegalArgumentException("Missing or invalid 'entityClassName'")

        val id = rootNode["id"]?.asText()?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Missing or invalid 'id'")

        val type = rootNode["type"]?.asText()?.let { EntityEventType.valueOf(it) }
            ?: throw IllegalArgumentException("Missing or invalid 'type'")

        val propertiesNode = rootNode["properties"]
            ?: throw IllegalArgumentException("Missing 'properties'")

        if (!propertiesNode.isObject) {
            throw IllegalArgumentException("'properties' must be a JSON object")
        }

        // Step 3: Resolve entity class dynamically using entityClassName
        val entityClass = resolveEntityClass(entityClassName)

        // Step 4: Deserialize properties
        val properties: Map<String, Any?> = try {
           objectMapper.convertValue(
                propertiesNode,
                objectMapper.typeFactory.constructMapType(MutableMap::class.java, String::class.java, entityClass)
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize 'properties' for entity class: $entityClassName", e)
        }

        // Step 5: Construct and return the EntityEvent object
        return EntityEvent(
            entityClassName = entityClassName,
            id = id,
            type = type,
            properties = properties
        )
    }

    private fun resolveEntityClass(entityClassName: String): Class<*> {
        val prefixedClassName = "_$entityClassName"
        try {
            val entityType = entityManager.metamodel.entities.find {
                it.javaType.simpleName == prefixedClassName
            } ?: throw IllegalArgumentException("Entity class not found for name: $prefixedClassName")

            return entityType.javaType
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Entity class not found for name: $prefixedClassName", e)
        }
    }
}
