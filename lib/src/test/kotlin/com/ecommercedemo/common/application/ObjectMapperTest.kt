package com.ecommercedemo.common.application


import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

data class DateTest @JsonCreator constructor(
    @JsonProperty("createdAt") val createdAt: LocalDateTime,
    @JsonProperty("updatedAt") val updatedAt: LocalDateTime
)

class ObjectMapperTest {

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build()) // Kotlin support
        registerModule(JavaTimeModule()) // Java Time support
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun `test deserialization of LocalDateTime`() {
        val json = """{
        "createdAt": "2024-11-30T02:23:43.004022757",
        "updatedAt": "2024-11-30T02:23:43.004044874"
    }"""

        val result = objectMapper.readValue(json, DateTest::class.java)
        assertEquals(LocalDateTime.of(2024, 11, 30, 2, 23, 43, 4022757), result.createdAt)
    }
}