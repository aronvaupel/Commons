package com.ecommercedemo.common.application.validation.email

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class EmailValidatorTest {

    private val validator = EmailValidator

    private fun mockContext(): ConstraintValidatorContext {
        return mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `isValid returns false for null or blank emails`() {
        val invalidEmails = listOf(null, "", "   ")

        invalidEmails.forEach { email ->
            val result = validator.isValid(email, mockContext())
            assertFalse(result, "Expected false for null or blank email: $email")
        }
    }

    @Test
    fun `isValid returns false for emails exceeding maximum length`() {
        val longEmail = "a".repeat(91) + "@example.com" // 101 characters
        val result = validator.isValid(longEmail, mockContext())
        assertFalse(result, "Expected false for email exceeding maximum length.")
    }

    @Test
    fun `isValid returns true for valid emails`() {
        val validEmails = listOf(
            "user@example.com",
            "user.name@example.com",
            "user_name@example.co.uk",
            "user-name+tag@example.org",
            "user.name123@example.info",
            "123user@example.io"
        )

        validEmails.forEach { email ->
            val result = validator.isValid(email, mockContext())
            assertTrue(result, "Expected true for valid email: $email")
        }
    }

    @Test
    fun `isValid returns false for invalid emails`() {
        val invalidEmails = listOf(
            "plainaddress", // Missing @
            "@missingusername.com", // Missing username
            "username@.com.my", // Leading dot in domain
            "username@example..com", // Multiple dots in domain
            "username@com", // Missing top-level domain
            "username@.com", // Missing domain name
            "username@site,com", // Invalid character in domain
            "username@site com", // Space in domain
            "username@", // Missing domain
            ".username@example.com", // Leading dot in username
            "username.@example.com", // Trailing dot in username
            "username@.example.com", // Dot at start of domain
            "username@example.com." // Dot at end of domain
        )

        invalidEmails.forEach { email ->
            val result = validator.isValid(email, mockContext())
            assertFalse(result, "Expected false for invalid email: $email")
        }
    }

}
