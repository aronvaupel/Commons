package com.ecommercedemo.common.controller.abstraction.util

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass

//Todo: Add support for more operators, i.e. regex, isNull, isNotNull, etc.
@Suppress("UNCHECKED_CAST", "unused")
enum class Operator(
    private val supportedTypes: Set<KClass<*>>
) {
    EQUALS(setOf(Any::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.equal(path, value)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.equal(jsonPathExpr, criteriaBuilder.literal(resolvedSearchParam.deserializedValue))
        }
    },
    NOT_EQUALS(setOf(Any::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notEqual(path, value)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.notEqual(jsonPathExpr, criteriaBuilder.literal(resolvedSearchParam.deserializedValue))
        }
    },
    CONTAINS(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "%$value%")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "%${resolvedSearchParam.deserializedValue}%")
        }
    },
    DOES_NOT_CONTAIN(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "%$value%")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$${resolvedSearchParam.deserializedValue}%"))
        }
    },
    STARTS_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "$value%")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "$${resolvedSearchParam.deserializedValue}%")
        }
    },
    DOES_NOT_START_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "$value%")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "$${resolvedSearchParam.deserializedValue}%"))
        }
    },
    ENDS_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "%$value")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$${resolvedSearchParam.deserializedValue}")
        }
    },
    DOES_NOT_END_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "%$value")

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$${resolvedSearchParam.deserializedValue}"))
        }
    },
    GREATER_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.greaterThan(jsonPathExpr as Expression<Comparable<Any>>, resolvedSearchParam.deserializedValue as Comparable<Any>)
        }
    },
    LESS_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.lessThan(jsonPathExpr as Expression<Comparable<Any>>, resolvedSearchParam.deserializedValue as Comparable<Any>)
        }
    },
    GREATER_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThanOrEqualTo(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.greaterThanOrEqualTo(
                jsonPathExpr as Expression<Comparable<Any>>,
                resolvedSearchParam.deserializedValue as Comparable<Any>
            )
        }
    },
    LESS_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThanOrEqualTo(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.lessThanOrEqualTo(
                jsonPathExpr as Expression<Comparable<Any>>,
                resolvedSearchParam.deserializedValue as Comparable<Any>
            )
        }
    },
    BEFORE(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.lessThan(jsonPathExpr as Expression<Comparable<Any>>, resolvedSearchParam.deserializedValue as Comparable<Any>)
        }
    },
    AFTER(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.greaterThan(jsonPathExpr as Expression<Comparable<Any>>, resolvedSearchParam.deserializedValue as Comparable<Any>)
        }
    },
    BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate {
            val (start, end) = restrictToTwoElements(value)
            return cb.between(path as Path<Comparable<Any>>, start, end)
        }

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val (start, end) = restrictToTwoElements(resolvedSearchParam.deserializedValue)
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.between(jsonPathExpr as Expression<Comparable<Any>>, start, end)
        }
    },
    NOT_BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate {
            val (start, end) = restrictToTwoElements(value)
            return cb.not(cb.between(path as Path<Comparable<Any>>, start, end))
        }

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val (start, end) = restrictToTwoElements(resolvedSearchParam.deserializedValue)
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.not(
                criteriaBuilder.between(jsonPathExpr as Expression<Comparable<Any>>, start, end)
            )
        }
    },
    ENUM_EQUALS(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.equal(path, value)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.equal(jsonPathExpr, criteriaBuilder.literal(resolvedSearchParam.deserializedValue))
        }
    },
    ENUM_NOT_EQUALS(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notEqual(path, value)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            return criteriaBuilder.notEqual(jsonPathExpr, criteriaBuilder.literal(resolvedSearchParam.deserializedValue))
        }
    },
    ENUM_IN(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            path.`in`(value as Collection<*>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).map { criteriaBuilder.literal(it) }
            return criteriaBuilder.`in`(jsonPathExpr).value(values)
        }
    },

    ENUM_NOT_IN(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(path.`in`(value as Collection<*>))

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).map { criteriaBuilder.literal(it) }
            return criteriaBuilder.not(criteriaBuilder.`in`(jsonPathExpr).value(values))
        }
    },

    IN(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            path.`in`(value as Collection<*>)

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).map { criteriaBuilder.literal(it.toString()) }
            return criteriaBuilder.`in`(jsonPathExpr).value(values)
        }
    },

    NOT_IN(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(path.`in`(value as Collection<*>))

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).map { criteriaBuilder.literal(it.toString()) }
            return criteriaBuilder.not(criteriaBuilder.`in`(jsonPathExpr).value(values))
        }
    },

    CONTAINS_ALL(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.and(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray())

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.equal(
                criteriaBuilder.function(
                    "jsonb_contains",
                    Boolean::class.java,
                    jsonPathExpr,
                    criteriaBuilder.literal("[$values]")
                ),
                criteriaBuilder.literal(true)
            )
        }
    },

    DOES_NOT_CONTAIN_ALL(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(cb.and(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }
                .toTypedArray()))

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.notEqual(
                criteriaBuilder.function(
                    "jsonb_contains",
                    Boolean::class.java,
                    jsonPathExpr,
                    criteriaBuilder.literal("[$values]")
                ),
                criteriaBuilder.literal(true)
            )
        }
    },

    CONTAINS_ANY(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.or(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray())

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.equal(
                criteriaBuilder.function(
                    "jsonb_contains_any",
                    Boolean::class.java,
                    jsonPathExpr,
                    criteriaBuilder.literal("[$values]")
                ),
                criteriaBuilder.literal(true)
            )
        }
    },

    DOES_NOT_CONTAIN_ANY(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(cb.or(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray()))

        override fun buildCondition(
            resolvedSearchParam: ResolvedSearchParam,
            criteriaBuilder: CriteriaBuilder,
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedSearchParam, criteriaBuilder)
            val values = (resolvedSearchParam.deserializedValue as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.notEqual(
                criteriaBuilder.function(
                    "jsonb_contains_any",
                    Boolean::class.java,
                    jsonPathExpr,
                    criteriaBuilder.literal("[$values]")
                ),
                criteriaBuilder.literal(true)
            )
        }
    };

    abstract fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate
    abstract fun buildCondition(
        resolvedSearchParam: ResolvedSearchParam,
        criteriaBuilder: CriteriaBuilder,
    ): Expression<Boolean>

    protected fun buildJsonPathExpression(
        resolvedSearchParam: ResolvedSearchParam,
        criteriaBuilder: CriteriaBuilder
    ): Expression<*> {
        var jsonPathExpr: Expression<*> = resolvedSearchParam.jpaPath
        resolvedSearchParam.jsonSegments.forEach { segment ->
            jsonPathExpr = criteriaBuilder.function(
                "jsonb_extract_path",
                String::class.java,
                jsonPathExpr,
                criteriaBuilder.literal(segment)
            )
        }
        return jsonPathExpr
    }

    fun isSupportedType(type: KClass<*>): Boolean {
        return supportedTypes.contains(type) || supportedTypes.contains(Any::class)
    }

    fun restrictToTwoElements(value: Any?): List<Comparable<Any>> {
        val collection = value as? Collection<*>
            ?: throw IllegalArgumentException("The operator requires a collection of two elements.")
        require(collection is List<*> && value.size == 2) {
            "BETWEEN operator requires exactly two elements in the list."
        }
        return collection.map { it as Comparable<Any> }
    }
}