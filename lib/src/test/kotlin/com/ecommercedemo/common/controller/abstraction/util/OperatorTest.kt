package com.ecommercedemo.common.controller.abstraction.util

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class OperatorTest {

    private val criteriaBuilder: CriteriaBuilder = mock(CriteriaBuilder::class.java)
    private val path: Path<Any> = mock(Path::class.java) as Path<Any>

    @Test
    fun `EQUALS operator builds predicate correctly`() {
        val operator = Operator.EQUALS
        val value = "testValue"

        `when`(criteriaBuilder.equal(path, value)).thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).equal(path, value)
        assertNotNull(predicate)
    }

    @Test
    fun `NOT_EQUALS operator builds predicate correctly`() {
        val operator = Operator.NOT_EQUALS
        val value = "testValue"

        `when`(criteriaBuilder.notEqual(path, value)).thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).notEqual(path, value)
        assertNotNull(predicate)
    }

    @Test
    fun `CONTAINS operator builds predicate correctly`() {
        val operator = Operator.CONTAINS
        val value = "test"

        `when`(criteriaBuilder.like(path as Path<String>, "%$value%")).thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).like(path as Path<String>, "%$value%")
        assertNotNull(predicate)
    }

    @Test
    fun `GREATER_THAN operator builds predicate correctly`() {
        val operator = Operator.GREATER_THAN
        val value = 5

        `when`(criteriaBuilder.greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>))
            .thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>)
        assertNotNull(predicate)
    }

    @Test
    fun `LESS_THAN operator builds predicate correctly`() {
        val operator = Operator.LESS_THAN
        val value = 5

        `when`(criteriaBuilder.lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>))
            .thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>)
        assertNotNull(predicate)
    }

    @Test
    fun `BETWEEN operator validates and builds predicate`() {
        val operator = Operator.BETWEEN
        val value = listOf(1, 10)

        `when`(
            criteriaBuilder.between(
                path as Path<Comparable<Any>>,
                value[0] as Comparable<Any>,
                value[1] as Comparable<Any>
            )
        )
            .thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).between(
            path as Path<Comparable<Any>>,
            value[0] as Comparable<Any>,
            value[1] as Comparable<Any>
        )
        assertNotNull(predicate)
    }

    @Test
    fun `BETWEEN operator throws exception for invalid collection size`() {
        val operator = Operator.BETWEEN
        val value = listOf(1)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            operator.buildPredicate(criteriaBuilder, path, value)
        }

        assertEquals("BETWEEN operator requires exactly two elements in the list.", exception.message)
    }

    @Test
    fun `IN operator builds predicate correctly`() {
        val operator = Operator.IN
        val value = listOf("item1", "item2", "item3")

        `when`(path.`in`(value)).thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(path).`in`(value)
        assertNotNull(predicate)
    }

    @Test
    fun `NOT_IN operator builds predicate correctly`() {
        val operator = Operator.NOT_IN
        val value = listOf("item1", "item2", "item3")

        `when`(criteriaBuilder.not(path.`in`(value))).thenReturn(mock(Predicate::class.java))

        val predicate = operator.buildPredicate(criteriaBuilder, path, value)

        verify(criteriaBuilder).not(path.`in`(value))
        assertNotNull(predicate)
    }
}

