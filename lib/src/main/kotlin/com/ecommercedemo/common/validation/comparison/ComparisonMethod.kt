package com.ecommercedemo.common.validation.comparison

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass


enum class ComparisonMethod(
    private val supportedTypes: Set<KClass<*>>
) {
    EQUALS(setOf(Any::class)),
    NOT_EQUALS(setOf(Any::class)),
    CONTAINS(setOf(String::class)),
    DOES_NOT_CONTAIN(setOf(String::class)),
    STARTS_WITH(setOf(String::class)),
    DOES_NOT_START_WITH(setOf(String::class)),
    ENDS_WITH(setOf(String::class)),
    DOES_NOT_END_WITH(setOf(String::class)),
    REGEX(setOf(String::class)),
    DOES_NOT_MATCH_REGEX(setOf(String::class)),
    GREATER_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    LESS_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    GREATER_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    LESS_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    NOT_GREATER_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    NOT_LESS_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)),
    BEFORE(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    AFTER(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    NOT_BEFORE(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    NOT_AFTER(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    NOT_BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)),
    ENUM_EQUALS(setOf(Enum::class)),
    ENUM_NOT_EQUALS(setOf(Enum::class)),
    ENUM_IN(setOf(Enum::class)),
    ENUM_NOT_IN(setOf(Enum::class)),
    IN(setOf(List::class, Set::class)),
    NOT_IN(setOf(List::class, Set::class)),
    CONTAINS_ALL(setOf(List::class, Set::class)),
    DOES_NOT_CONTAIN_ALL(setOf(List::class, Set::class)),
    CONTAINS_ANY(setOf(List::class, Set::class)),
    DOES_NOT_CONTAIN_ANY(setOf(List::class, Set::class));

    fun isSupportedType(type: KClass<*>): Boolean {
        return supportedTypes.contains(type) || supportedTypes.contains(Any::class)
    }
}