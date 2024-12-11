package com.ecommercedemo.common.application

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper

class PropertiesDeserializer : JsonDeserializer<Map<String, Any?>>() {
    override fun deserialize(
        parser: JsonParser,
        ctxt: DeserializationContext
    ): Map<String, Any?> {
        val objectMapper = parser.codec as ObjectMapper
        return objectMapper.readValue(
            parser,
            objectMapper.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
        )
    }
}