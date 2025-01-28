package com.ecommercedemo.common.application.validation.name

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class NameValidatorTest {

    private val validator = NameValidator

    private fun mockContext(): ConstraintValidatorContext {
        return mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `isValid returns false for null or blank names`() {
        val invalidNames = listOf(null, "", "   ")

        invalidNames.forEach { name ->
            val result = validator.isValid(name, mockContext())
            assertFalse(result, "Expected false for null or blank name: $name")
        }
    }

    @Test
    fun `isValid returns false for names exceeding maximum length`() {
        val longName = "A".repeat(51)
        val result = validator.isValid(longName, mockContext())
        assertFalse(result, "Expected false for name exceeding maximum length.")
    }

    @Test
    fun `isValid returns true for valid names`() {
        val validNames = listOf(
            "John Doe",
            "Jane Smith",
            "O'Connor",
            "Anne-Marie",
            "Name With Spaces",
            "Hyphenated-Name"
        )

        validNames.forEach { name ->
            val result = validator.isValid(name, mockContext())
            assertTrue(result, "Expected true for valid name: $name")
        }
    }

    @Test
    fun `isValid returns false for names with invalid characters`() {
        val invalidNames = listOf(
            "John@Doe",  // Special character "@"
            "Jane#Smith",  // Special character "#"
            "O'Connor123",  // Digits in name
            "Name_With_Underscore",  // Underscore
            "Name!",  // Special character "!"
            "Name With 123",  // Digits in the middle
        )

        invalidNames.forEach { name ->
            val result = validator.isValid(name, mockContext())
            assertFalse(result, "Expected false for invalid name: $name")
        }
    }

    @Test
    fun `isValid returns true for names at maximum length`() {
        val maxLengthName = "A".repeat(50)
        val result = validator.isValid(maxLengthName, mockContext())
        assertTrue(result, "Expected true for name of exactly maximum length.")
    }
}
