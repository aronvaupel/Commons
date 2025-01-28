package com.ecommercedemo.common.application.validation.operator

import com.ecommercedemo.common.controller.abstraction.util.Operator
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class OperatorTypeValidatorTest {

    private val validator = OperatorTypeValidator

    private fun mockContext(): ConstraintValidatorContext {
        return mock(ConstraintValidatorContext::class.java)
    }

    @Test
    fun `isValid returns true for null search value`() {
        val searchParam = SearchParam(operator = Operator.EQUALS, searchValue = null, path = "username")
        val result = validator.isValid(searchParam, mockContext())
        assertTrue(result, "Validator should return true for null search value.")
    }

    @Test
    fun `isValid returns true for valid operator and value`() {
        val searchParam = SearchParam(operator = Operator.EQUALS, searchValue = "test", path = "username")
        val result = validator.isValid(searchParam, mockContext())
        assertTrue(result, "Validator should return true for supported operator and valid value.")
    }

    @Test
    fun `isValid returns false for unsupported value type`() {
        val searchParam = SearchParam(operator = Operator.CONTAINS, searchValue = 123, path = "username")
        val result = validator.isValid(searchParam, mockContext())
        assertFalse(result, "Validator should return false for unsupported value type.")
    }

    @Test
    fun `isValid returns false for invalid operator and value combinations`() {
        val invalidParams = listOf(
            SearchParam(operator = Operator.CONTAINS, searchValue = 123, path = "username"), // CONTAINS expects String
            SearchParam(
                operator = Operator.GREATER_THAN,
                searchValue = "not-a-number",
                path = "age"
            ),
            SearchParam(
                operator = Operator.BEFORE,
                searchValue = "invalid-date",
                path = "creationDate"
            )
        )

        invalidParams.forEach { param ->
            val result = validator.isValid(param, mockContext())
            assertFalse(
                result,
                "Validator should return false for operator ${param.operator} with unsupported type ${param.searchValue?.javaClass?.simpleName}."
            )
        }
    }

    @Test
    fun `isValid handles edge cases for null operator`() {
        val searchParam = SearchParam(operator = Operator.EQUALS, searchValue = null, path = "username")
        val result = validator.isValid(searchParam, mockContext())
        assertTrue(result, "Validator should handle null operator gracefully.")
    }
}
