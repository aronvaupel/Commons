package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.util.search.dto.ResolvedPathInfo
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
enum class Operator(
    private val supportedTypes: Set<KClass<*>>
) {
    EQUALS(setOf(Any::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.equal(path, value)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.equal(jsonPathExpr, criteriaBuilder.literal(value))
        }
    },
    NOT_EQUALS(setOf(Any::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notEqual(path, value)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.notEqual(jsonPathExpr, criteriaBuilder.literal(value))
        }
    },
    CONTAINS(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "%$value%")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$value%")
        }
    },
    DOES_NOT_CONTAIN(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "%$value%")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$value%"))
        }
    },
    STARTS_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "$value%")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "$value%")
        }
    },
    DOES_NOT_START_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "$value%")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "$value%"))
        }
    },
    ENDS_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.like(path as Path<String>, "%$value")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$value")
        }
    },
    DOES_NOT_END_WITH(setOf(String::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notLike(path as Path<String>, "%$value")

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.not(criteriaBuilder.like(jsonPathExpr as Expression<String>, "%$value"))
        }
    },
    GREATER_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.greaterThan(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    LESS_THAN(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.lessThan(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    GREATER_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThanOrEqualTo(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.greaterThanOrEqualTo(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    LESS_THAN_OR_EQUAL(setOf(Int::class, Long::class, Short::class, Double::class, Float::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThanOrEqualTo(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.lessThanOrEqualTo(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    BEFORE(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.lessThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.lessThan(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    AFTER(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.greaterThan(path as Path<Comparable<Any>>, value as Comparable<Any>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.greaterThan(jsonPathExpr as Expression<Comparable<Any>>, value as Comparable<Any>)
        }
    },
    BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate {
            val (start, end) = value as List<Comparable<Any>>
            return cb.between(path as Path<Comparable<Any>>, start, end)
        }

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val (start, end) = value as Pair<Comparable<Any>, Comparable<Any>>
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.between(jsonPathExpr as Expression<Comparable<Any>>, start, end)
        }
    },
    NOT_BETWEEN(setOf(LocalDate::class, LocalDateTime::class, Instant::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate {
            val (start, end) = value as List<Comparable<Any>>
            return cb.not(cb.between(path as Path<Comparable<Any>>, start, end))
        }

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val (start, end) = value as Pair<Comparable<Any>, Comparable<Any>>
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.not(
                criteriaBuilder.between(jsonPathExpr as Expression<Comparable<Any>>, start, end)
            )
        }
    },
    ENUM_EQUALS(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.equal(path, value)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.equal(jsonPathExpr, criteriaBuilder.literal(value))
        }
    },
    ENUM_NOT_EQUALS(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.notEqual(path, value)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            return criteriaBuilder.notEqual(jsonPathExpr, criteriaBuilder.literal(value))
        }
    },
    ENUM_IN(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            path.`in`(value as Collection<*>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).map { criteriaBuilder.literal(it) }
            return criteriaBuilder.`in`(jsonPathExpr).value(values)
        }
    },

    ENUM_NOT_IN(setOf(Enum::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(path.`in`(value as Collection<*>))

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).map { criteriaBuilder.literal(it) }
            return criteriaBuilder.not(criteriaBuilder.`in`(jsonPathExpr).value(values))
        }
    },

    IN(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            path.`in`(value as Collection<*>)

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).map { criteriaBuilder.literal(it.toString()) }
            return criteriaBuilder.`in`(jsonPathExpr).value(values)
        }
    },

    NOT_IN(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(path.`in`(value as Collection<*>))

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).map { criteriaBuilder.literal(it.toString()) }
            return criteriaBuilder.not(criteriaBuilder.`in`(jsonPathExpr).value(values))
        }
    },

    CONTAINS_ALL(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.and(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray())

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.equal(
                criteriaBuilder.function("jsonb_contains", Boolean::class.java, jsonPathExpr, criteriaBuilder.literal("[$values]")),
                criteriaBuilder.literal(true)
            )
        }
    },

    DOES_NOT_CONTAIN_ALL(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(cb.and(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray()))

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.notEqual(
                criteriaBuilder.function("jsonb_contains", Boolean::class.java, jsonPathExpr, criteriaBuilder.literal("[$values]")),
                criteriaBuilder.literal(true)
            )
        }
    },

    CONTAINS_ANY(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.or(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray())

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.equal(
                criteriaBuilder.function("jsonb_contains_any", Boolean::class.java, jsonPathExpr, criteriaBuilder.literal("[$values]")),
                criteriaBuilder.literal(true)
            )
        }
    },

    DOES_NOT_CONTAIN_ANY(setOf(List::class, Set::class)) {
        override fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate =
            cb.not(cb.or(*(value as Collection<*>).map { cb.isMember(it, path as Path<Collection<*>>) }.toTypedArray()))

        override fun buildCondition(
            resolvedPathInfo: ResolvedPathInfo,
            criteriaBuilder: CriteriaBuilder,
            value: Any?
        ): Expression<Boolean> {
            val jsonPathExpr = buildJsonPathExpression(resolvedPathInfo, criteriaBuilder)
            val values = (value as Collection<*>).joinToString(",") { "\"$it\"" }
            return criteriaBuilder.notEqual(
                criteriaBuilder.function("jsonb_contains_any", Boolean::class.java, jsonPathExpr, criteriaBuilder.literal("[$values]")),
                criteriaBuilder.literal(true)
            )
        }
    };

    abstract fun buildPredicate(cb: CriteriaBuilder, path: Path<*>, value: Any?): Predicate
    abstract fun buildCondition(
        resolvedPathInfo: ResolvedPathInfo,
        criteriaBuilder: CriteriaBuilder,
        value: Any?
    ): Expression<Boolean>

    protected fun buildJsonPathExpression(
        resolvedPathInfo: ResolvedPathInfo,
        criteriaBuilder: CriteriaBuilder
    ): Expression<*> {
        var jsonPathExpr: Expression<*> = resolvedPathInfo.jpaPath
        resolvedPathInfo.jsonSegments.forEach { segment ->
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
}