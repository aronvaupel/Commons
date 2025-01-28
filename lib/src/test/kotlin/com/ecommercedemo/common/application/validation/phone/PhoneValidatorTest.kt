package com.ecommercedemo.common.application.validation.phone

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhoneValidatorTest {

    private val validator = PhoneValidator

    @Test
    fun `isValid returns true for null phone number`() {
        val result = validator.isValid(null, mockContext())
        assertTrue(result, "Validator should return true for null phone numbers.")
    }

    @Test
    fun `isValid returns true for valid phone numbers`() {
        val validPhoneNumbers = listOf(
            "+1234567890",
            "1234567890",
            "+19876543210",
            "+123456789012345"
        )

        validPhoneNumbers.forEach { phone ->
            val result = validator.isValid(phone, mockContext())
            assertTrue(result, "Expected true for valid phone number: $phone")
        }
    }

    @Test
    fun `isValid returns false for invalid phone numbers`() {
        val invalidPhoneNumbers = listOf(
            "+1234567890123456", // Exceeds max length
            "0123456789", // Starts with 0
            "+0123456789", // Starts with 0 even after "+"
            "abcdefg", // Non-numeric characters
            "+12345abc678", // Mixed invalid characters
            "+", // Only "+"
            "", // Empty string
            " " // Only spaces
        )

        invalidPhoneNumbers.forEach { phone ->
            val result = validator.isValid(phone, mockContext())
            assertFalse(result, "Expected false for invalid phone number: $phone")
        }
    }

    private fun mockContext(): ConstraintValidatorContext {
        return org.mockito.Mockito.mock(ConstraintValidatorContext::class.java)
    }
}