package com.ecommercedemo.common.application.springboot

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UnusedAnnotation

@TestAnnotation
class AnnotatedClass1

@TestAnnotation
class AnnotatedClass2

class NonAnnotatedClass

class ClassPathScannerTest {

    private val classPathScanner = ClassPathScanner()

    @Test
    fun `findClassesWithAnnotation should return classes with the specified annotation`() {
        val result = classPathScanner.findClassesWithAnnotation(TestAnnotation::class)

        assertEquals(2, result.size)
        assertTrue(result.contains(AnnotatedClass1::class.java))
        assertTrue(result.contains(AnnotatedClass2::class.java))
    }

    @Test
    fun `findClassesWithAnnotation should return empty set for unused annotation`() {
        val result = classPathScanner.findClassesWithAnnotation(UnusedAnnotation::class)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findClassesWithAnnotation should ignore non-annotated classes`() {
        val result = classPathScanner.findClassesWithAnnotation(TestAnnotation::class)

        assertFalse(result.contains(NonAnnotatedClass::class.java))
    }
}