package com.ecommercedemo.common.controller.abstraction.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SearchParamConverterTest {

    private val objectMapper = ObjectMapper()
    private val converter = SearchParamConverter(objectMapper)

    @Test
    fun `convertAnyIfNeeded returns the same value if it matches the expected type`() {
        val value = "test"
        val result = converter.convertAnyIfNeeded(value, String::class.java)
        assertEquals(value, result)
    }

    @Test
    fun `convertAnyIfNeeded converts value to the expected type if it does not match`() {
        val value = 123
        val result = converter.convertAnyIfNeeded(value, String::class.java)
        assertEquals("123", result)
    }

    @Test
    fun `convertAnyIfNeeded converts a collection of values to the expected type`() {
        val value = listOf(1, 2, 3)
        val result = converter.convertAnyIfNeeded(value, String::class.java)
        assertTrue(result is List<*>)
        assertEquals(listOf("1", "2", "3"), result)
    }

    @Test
    fun `convertAnyIfNeeded handles null values`() {
        val result = converter.convertAnyIfNeeded(null, String::class.java)
        assertNull(result)
    }

    @Test
    fun `convertIfNeeded returns the same value if it matches the expected type`() {
        val value = 42
        val result = converter.convertAnyIfNeeded(value, Int::class.java)
        assertEquals(value, result)
    }

    @Test
    fun `convertIfNeeded converts value to the expected type if it does not match`() {
        val value = "true"
        val result = converter.convertAnyIfNeeded(value, Boolean::class.java)
        assertEquals(true, result)
    }


    @Test
    fun `convertAnyIfNeeded handles mixed-type collections`() {
        val value = listOf("1", 2, "3.0")
        val result = converter.convertAnyIfNeeded(value, Double::class.java)
        assertTrue(result is List<*>)
        assertEquals(listOf(1.0, 2.0, 3.0), result)
    }
}
