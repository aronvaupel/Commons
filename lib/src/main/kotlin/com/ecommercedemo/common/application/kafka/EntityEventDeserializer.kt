package com.ecommercedemo.common.application.kafka

import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.model.concretion.permissionuserassociation.PermissionUserAssociation
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*


class EntityEventDeserializer(
    private val objectMapper: ObjectMapper,
    private val typeReAttacher: TypeReAttacher
) : JsonDeserializer<EntityEvent>() {


    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): EntityEvent {
        val rootNode = parser.codec.readTree<ObjectNode>(parser)

        val entityClassName = rootNode["entityClassName"]?.asText()
            ?: throw IllegalArgumentException("Missing or invalid 'entityClassName'")

        val id = rootNode["id"]?.asText()?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Missing or invalid 'id'")

        val type = rootNode["type"]?.asText()?.let { ModificationType.valueOf(it) }
            ?: throw IllegalArgumentException("Missing or invalid 'type'")

        val propertiesNode = rootNode["properties"]
            ?: throw IllegalArgumentException("Missing 'properties'")

        if (!propertiesNode.isObject) {
            throw IllegalArgumentException("Properties' must be a JSON object")
        }

        val normalizedClassName =
            if (entityClassName == PermissionUserAssociation::class.java.simpleName)
                PermissionUserAssociation::class.java.simpleName
            else "_$entityClassName"
        val properties: Map<String, Any?> = try {
            val rawData: Map<String, Any?> = try {
                objectMapper.readValue(propertiesNode.traverse(), object : TypeReference<Map<String, Any?>>() {})
            } catch (e: Exception) {
                throw RuntimeException("Failed to deserialize 'properties'", e)
            }

            typeReAttacher.reAttachType(rawData, normalizedClassName)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize 'properties' for entity class: $normalizedClassName", e)
        }

        val result = EntityEvent(
            entityClassName = entityClassName,
            id = id,
            type = type,
            properties = properties
        )
        println("Deserialized entity event: $result")
        return result
    }

}
