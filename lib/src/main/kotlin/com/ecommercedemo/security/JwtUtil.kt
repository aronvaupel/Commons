package com.ecommercedemo.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

object JwtUtil {
    private const val SECRET_KEY = "your_secret_key"

    fun generateToken(username: String, expiration: Long): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).body.subject
    }
}