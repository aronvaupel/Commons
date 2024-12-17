package com.ecommercedemo.common.application.exception

import jakarta.validation.ConstraintViolationException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.management.AttributeNotFoundException

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(AttributeNotFoundException::class)
    fun handleAttributeNotFoundException(ex: AttributeNotFoundException): ResponseEntity<MultiValueMap<String, String>> {
        log.warn("Invalid attribute error: ${ex.message}")
        log.debug { ex.printStackTrace() }
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("error", "Invalid Attribute")
        body.add("message", ex.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ValueTypeMismatchException::class)
    fun handleValueTypeMismatchException(ex: ValueTypeMismatchException): ResponseEntity<MultiValueMap<String, String>> {
        log.warn("Type mismatch error: ${ex.message}")
        log.debug { ex.printStackTrace() }
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("error", "Type Mismatch")
        body.add("message", ex.message ?: "Unknown error")
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, String>> {
        log.warn("Unexpected error: ${ex.message}", ex)
        log.debug { ex.printStackTrace() }
        return ResponseEntity(
            mapOf("error" to "Internal Server Error", "message" to "${ex.message}"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        log.warn("Exception caught in GlobalExceptionHandler: ${ex.message}")
        log.debug { ex.printStackTrace() }
        val errorResponse = mapOf(
            "error" to (ex.message ?: "Invalid request"),
            "details" to "An illegal argument was provided. Please check the request payload or parameters."
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Validation error") }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<Map<String, String>> {
        val errors = ex.constraintViolations.associate { it.propertyPath.toString() to (it.message ?: "Constraint violation") }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }
}