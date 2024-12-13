package com.ecommercedemo.common.application.kafka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityManager
import java.util.*

class EntityEventDeserializer(
    private var objectMapper: ObjectMapper,
    private var entityManager: EntityManager
) : JsonDeserializer<EntityEvent>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): EntityEvent {
        val rootNode = parser.codec.readTree<ObjectNode>(parser)

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

        val entityClass = resolveEntityClass(entityClassName)

        val properties: Map<String, Any?> = try {
            objectMapper.convertValue(
                propertiesNode,
                objectMapper.typeFactory.constructMapType(MutableMap::class.java, String::class.java, entityClass)
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize 'properties' for entity class: $entityClassName", e)
        }

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
