package com.ecommercedemo.common.application.kafka

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityManager


class EventPropertiesDeserializer(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper
) : JsonDeserializer<EntityEvent>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): EntityEvent {
        val parentNode = parser.codec.readTree<ObjectNode>(parser)

        val entityClassName = parentNode.get("entityClassName").asText()

        val entityClass = resolveEntityClass(entityClassName)

        val propertiesNode = parentNode.get("properties")
            ?: throw IllegalArgumentException("Missing 'properties' field in JSON")

        return try {
            objectMapper.convertValue(
                propertiesNode,
                objectMapper.typeFactory.constructMapType(MutableMap::class.java, String::class.java, entityClass)
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize 'properties' for entity class: $entityClassName", e)
        }

    }

    private fun resolveEntityClass(entityClassName: String): Class<*> {
        val downstreamEntityClassName = "_$entityClassName"
        try {
            val entityType = entityManager.metamodel.entities.find {
                it.javaType.simpleName == downstreamEntityClassName
            } ?: throw IllegalArgumentException("Entity class not found for name: $downstreamEntityClassName")

            return entityType.javaType
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Entity class not found for name: $downstreamEntityClassName", e)
        }
    }
}
