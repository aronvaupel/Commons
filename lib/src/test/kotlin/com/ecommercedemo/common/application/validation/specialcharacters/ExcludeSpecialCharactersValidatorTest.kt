package com.ecommercedemo.common.application.validation.specialcharacters

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExcludeSpecialCharactersValidatorTest {

    private val validator = ExcludeSpecialCharactersValidator

    @Test
    fun `isValid returns true for null value`() {
        val result = validator.isValid(null, mockContext())
        assertTrue(result, "Validator should return true for null values.")
    }

    @Test
    fun `isValid returns true for valid string`() {
        val validInputs = listOf(
            "SimpleText",
            "Text with spaces",
            "1234567890",
            "Text123",
            "Valid-text, with/slashes.and(parentheses)"
        )

        validInputs.forEach { input ->
            val result = validator.isValid(input, mockContext())
            assertTrue(result, "Expected true for valid input: $input")
        }
    }

    @Test
    fun `isValid returns false for string with special characters`() {
        val invalidInputs = listOf(
            "Invalid@Text",
            "Contains#Hash",
            "Invalid\$Dollar",
            "Special*Characters!",
            "Emoji🙂"
        )

        invalidInputs.forEach { input ->
            val result = validator.isValid(input, mockContext())
            assertFalse(result, "Expected false for invalid input: $input")
        }
    }

    @Test
    fun `isValid allows valid punctuation characters`() {
        val validInputs = listOf(
            "Text-with-hyphen",
            "Comma,separated",
            "Period.ending",
            "Slash/forward",
            "Parentheses()"
        )

        validInputs.forEach { input ->
            val result = validator.isValid(input, mockContext())
            assertTrue(result, "Expected true for valid input: $input")
        }
    }

    @Test
    fun `isValid handles empty string`() {
        val result = validator.isValid("", mockContext())
        assertTrue(result, "Validator should return true for an empty string.")
    }

    @Test
    fun `isValid handles string with only spaces`() {
        val result = validator.isValid("   ", mockContext())
        assertTrue(result, "Validator should return true for strings with only spaces.")
    }

    private fun mockContext(): ConstraintValidatorContext {
        return org.mockito.Mockito.mock(ConstraintValidatorContext::class.java)
    }
}
