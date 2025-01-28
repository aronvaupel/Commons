package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.exception.ValueTypeMismatchException
import com.ecommercedemo.common.service.concretion.ReflectionService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.reflect.full.memberProperties

class SearchParamValidationTest {

    private val reflectionService: ReflectionService = mock(ReflectionService::class.java)
    private val deserializer: SearchParamConverter = mock(SearchParamConverter::class.java)
    private val validation = SearchParamValidation(deserializer, reflectionService)

    @Test
    fun `validate throws ValueTypeMismatchException for null value in non-nullable field`() {
        val attributePath = "testField"

        `when`(reflectionService.getClassMemberProperties(MyTestClass::class))
            .thenReturn(MyTestClass::class.memberProperties)

        val exception = assertThrows<ValueTypeMismatchException> {
            validation.validate(null, String::class.java, MyTestClass::class, attributePath)
        }

        assertTrue(
            exception.message!!.contains("Type mismatch for '$attributePath': expected String, got null"),
            "Unexpected exception message: ${exception.message}"
        )
    }


    @Test
    fun `validate throws ValueTypeMismatchException for incorrect type`() {
        val attributePath = "testField"

        val exception = assertThrows<ValueTypeMismatchException> {
            validation.validate(123, String::class.java, MyTestClass::class, attributePath)
        }

        assertTrue(
            exception.message!!.contains("Type mismatch for '$attributePath': expected String, got Int"),
            "Unexpected exception message: ${exception.message}"
        )
    }

    @Test
    fun `validate does not throw for valid type`() {
        val attributePath = "testField"

        assertDoesNotThrow {
            validation.validate("test", String::class.java, MyTestClass::class, attributePath)
        }
    }

    @Test
    fun `validateCollectionElements throws ValueTypeMismatchException for invalid collection element`() {
        val attributePath = "testCollection"

        val exception = assertThrows<ValueTypeMismatchException> {
            validation.validate(listOf("valid", 123), String::class.java, MyTestClass::class, attributePath)
        }

        assertTrue(
            exception.message!!.contains("Type mismatch for '$attributePath': expected String, got Int"),
            "Unexpected exception message: ${exception.message}"
        )
    }

    @Test
    fun `validateFieldExistsAndIsAccessible does not throw for existing field`() {
        val segment = "existingField"
        val currentClass = MyTestClassWithField::class.java

        assertDoesNotThrow {
            validation.validateFieldExistsAndIsAccessible(segment, currentClass)
        }
    }

    // Sample Test Classes
    data class MyTestClass(
        val testField: String,
        val nullableField: String?
    )

    class MyTestClassWithField {
        val existingField: String = "test"
    }
}
