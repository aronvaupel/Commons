package com.ecommercedemo.common.application.validation.password

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PasswordCryptoTest {

    @Test
    fun `hashPassword generates a valid hash`() {
        val rawPassword = "Test@123"
        val hashedPassword = PasswordCrypto.hashPassword(rawPassword)

        assertNotNull(hashedPassword, "Hashed password should not be null.")
        assertTrue(hashedPassword.startsWith("\$2a\$"), "Hashed password should start with bcrypt prefix '\$2a\$'.")
    }

    @Test
    fun `matches returns true for correct raw and hashed passwords`() {
        val rawPassword = "Test@123"
        val hashedPassword = PasswordCrypto.hashPassword(rawPassword)

        val isMatch = PasswordCrypto.matches(rawPassword, hashedPassword)
        assertTrue(isMatch, "PasswordCrypto.matches should return true for matching raw and hashed passwords.")
    }

    @Test
    fun `matches returns false for incorrect raw and hashed passwords`() {
        val rawPassword = "Test@123"
        val hashedPassword = PasswordCrypto.hashPassword("DifferentPassword")

        val isMatch = PasswordCrypto.matches(rawPassword, hashedPassword)
        assertFalse(isMatch, "PasswordCrypto.matches should return false for non-matching raw and hashed passwords.")
    }

    @Test
    fun `hashPassword generates unique hashes for the same password`() {
        val rawPassword = "Test@123"
        val hashedPassword1 = PasswordCrypto.hashPassword(rawPassword)
        val hashedPassword2 = PasswordCrypto.hashPassword(rawPassword)

        assertNotEquals(
            hashedPassword1,
            hashedPassword2,
            "Hashes for the same password should be unique due to salting."
        )
    }
}
