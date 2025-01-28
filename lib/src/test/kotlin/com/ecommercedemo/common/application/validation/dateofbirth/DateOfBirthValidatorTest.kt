package com.ecommercedemo.common.application.validation.dateofbirth

import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.time.LocalDate

class DateOfBirthValidatorTest {

    private val validator = DateOfBirthValidator

    private fun mockContext(): ConstraintValidatorContext {
        return mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `isValid returns false for null value`() {
        val result = validator.isValid(null, mockContext())
        assertFalse(result, "Validator should return false for null date.")
    }

    @Test
    fun `isValid returns false for future dates`() {
        val futureDate = LocalDate.now().plusDays(1)
        val result = validator.isValid(futureDate, mockContext())
        assertFalse(result, "Validator should return false for future dates.")
    }

    @Test
    fun `isValid returns false for dates older than 120 years`() {
        val oldDate = LocalDate.now().minusYears(121)
        val result = validator.isValid(oldDate, mockContext())
        assertFalse(result, "Validator should return false for dates older than 120 years.")
    }

    @Test
    fun `isValid returns true for valid dates of birth`() {
        val validDates = listOf(
            LocalDate.now().minusYears(30),
            LocalDate.now().minusYears(120).plusDays(1),
            LocalDate.now().minusYears(1)
        )

        validDates.forEach { date ->
            val result = validator.isValid(date, mockContext())
            assertTrue(result, "Expected true for valid date of birth: $date")
        }
    }

    @Test
    fun `isValid returns false for exactly 120 years ago`() {
        val boundaryDate = LocalDate.now().minusYears(120)
        val result = validator.isValid(boundaryDate, mockContext())
        assertFalse(result, "Validator should return false for dates exactly 120 years ago.")
    }
}
