package com.ecommercedemo.common.validation.password

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object PasswordCrypto {
    private val encoder = BCryptPasswordEncoder()

    fun hashPassword(password: String): String {
        return encoder.encode(password)
    }

    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return encoder.matches(rawPassword, encodedPassword)
    }
}