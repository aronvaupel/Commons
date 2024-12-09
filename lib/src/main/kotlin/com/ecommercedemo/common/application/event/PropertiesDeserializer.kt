package com.ecommercedemo.common.application.event

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.*

class PropertiesDeserializer : JsonDeserializer<MutableMap<String, Any?>>() {
    override fun deserialize(
        parser: JsonParser,
        ctxt: DeserializationContext
    ): MutableMap<String, Any?> {
        val objectMapper = parser.codec as ObjectMapper
        val node = parser.readValueAsTree<com.fasterxml.jackson.databind.node.ObjectNode>()
        val result = mutableMapOf<String, Any?>()

        node.fields().forEachRemaining { (key, value) ->
            result[key] = parseIsoStringIfApplicable(value) ?: objectMapper.treeToValue(value, Any::class.java)
        }
        return result
    }

    private fun parseIsoStringIfApplicable(value: com.fasterxml.jackson.databind.JsonNode): Any? {
        if (!value.isTextual) return null
        val text = value.asText()

        return when {
            text.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z")) -> Instant.parse(text)
            text.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) -> LocalDateTime.parse(text)
            text.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> LocalDate.parse(text)
            text.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*[+-]\\d{2}:\\d{2}(\\[\\w+/\\w+])?")) -> ZonedDateTime.parse(text)
            text.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")) -> OffsetDateTime.parse(text)
            text.matches(Regex("\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")) -> OffsetTime.parse(text)
            text.startsWith("P") && text.contains("T") -> Duration.parse(text)
            text.startsWith("P") -> Period.parse(text)
            else -> null
        }
    }
}
