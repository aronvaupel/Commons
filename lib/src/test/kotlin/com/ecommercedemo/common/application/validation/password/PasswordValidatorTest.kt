package com.ecommercedemo.common.application.validation.password

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordValidatorTest {

    private val validator = PasswordValidator

    @Test
    fun `isValid returns false for null or blank passwords`() {
        val invalidPasswords = listOf(null, "", "   ")

        invalidPasswords.forEach { password ->
            val result = validator.isValid(password, mockContext())
            assertFalse(result, "Expected false for null or blank password: $password")
        }
    }

    @Test
    fun `isValid returns false for passwords exceeding maximum length`() {
        val longPassword = "A1@a".repeat(26) // 104 characters
        val result = validator.isValid(longPassword, mockContext())
        assertFalse(result, "Expected false for password exceeding maximum length")
    }

    @Test
    fun `isValid returns true for valid passwords`() {
        val validPasswords = listOf(
            "Valid1@Password",
            "Another#Pass123",
            "Complex@1234ABC",
            "P@ssw0rd!"
        )

        validPasswords.forEach { password ->
            val result = validator.isValid(password, mockContext())
            assertTrue(result, "Expected true for valid password: $password")
        }
    }

    @Test
    fun `isValid returns false for passwords not meeting complexity requirements`() {
        val invalidPasswords = listOf(
            "short1@", // Too short
            "NoSpecialChar1", // Missing special character
            "NoNumber@", // Missing digit
            "NOLOWERCASE1@", // Missing lowercase
            "nouppercase1@", // Missing uppercase
            "Contains Space1@", // Contains whitespace
            "P@$\$word" // Too simple and missing a digit
        )

        invalidPasswords.forEach { password ->
            val result = validator.isValid(password, mockContext())
            assertFalse(result, "Expected false for invalid password: $password")
        }
    }

    @Test
    fun `isValid returns true for passwords of exactly maximum length`() {
        val validLongPassword = "A1@a".repeat(25) // 100 characters
        val result = validator.isValid(validLongPassword, mockContext())
        assertTrue(result, "Expected true for password of exactly maximum length")
    }

    private fun mockContext(): ConstraintValidatorContext {
        return org.mockito.Mockito.mock(ConstraintValidatorContext::class.java)
    }
}
