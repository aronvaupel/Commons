package com.ecommercedemo.common.util.filter


import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.redis.RedisService
import com.ecommercedemo.common.validation.comparison.ComparisonMethod
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST", "unused")
@Service
class QueryBuilder<T : Any>(
    private val entityManager: EntityManager,
    private val redisCacheService: RedisService
) {

    fun buildQuery(entityClass: KClass<T>, queryParameters: QueryParams<T>): List<T> {
        val queryKey = redisCacheService.generateQueryKey(entityClass, queryParameters)
        redisCacheService.getQueryResult(queryKey)?.let { return it as List<T> }

        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entityClass.java)
        val root = criteriaQuery.from(entityClass.java)

        val predicates = queryParameters.filters.map { filter ->
            buildPredicateRecursively(criteriaBuilder, root, entityClass, filter)
        }

        criteriaQuery.where(*predicates.toTypedArray())
        val result = entityManager.createQuery(criteriaQuery).resultList
        redisCacheService.cacheQueryResult(queryKey, result)
        return result
    }

    private fun buildPredicateRecursively(
        criteriaBuilder: CriteriaBuilder,
        root: Root<T>,
        entityClass: KClass<T>,
        filter: FilterCriteria<T>
    ): Predicate {
        if (filter.nestedFilters.isNotEmpty()) {
            val nestedPredicates = filter.nestedFilters.map { nestedFilter ->
                buildPredicateRecursively(criteriaBuilder, root, entityClass, nestedFilter)
            }
            return criteriaBuilder.and(*nestedPredicates.toTypedArray())
        }

        val path = validateAndGetPath(entityClass, root, filter.attribute)
        return createPredicate(criteriaBuilder, path, filter)
    }

    private fun <T : Any> validateAndGetPath(
        entityClass: KClass<T>,
        root: Root<T>,
        attribute: String
    ): Path<*> {
        val pathSegments = attribute.split(".")
        var currentPath: Path<*> = root
        var currentClass: KClass<*> = entityClass

        for (segment in pathSegments) {
            val property = currentClass.memberProperties.find { it.name == segment }
                ?: throw InvalidAttributeException(
                    segment, currentClass.simpleName ?: "UnknownEntity"
                )

            currentPath = currentPath.get<Any>(segment)
            currentClass = property.returnType.classifier as? KClass<*>
                ?: throw IllegalArgumentException(
                    "Could not determine class type for $segment in $attribute"
                )
        }

        return currentPath
    }

    private fun createPredicate(
        criteriaBuilder: CriteriaBuilder,
        path: Path<*>,
        filter: FilterCriteria<T>
    ): Predicate {
        return when (filter.comparison) {
            ComparisonMethod.EQUALS -> criteriaBuilder.equal(path, filter.value)
            ComparisonMethod.NOT_EQUALS -> criteriaBuilder.notEqual(path, filter.value)
            ComparisonMethod.GREATER_THAN -> criteriaBuilder.greaterThan(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.NOT_GREATER_THAN -> criteriaBuilder.lessThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.LESS_THAN -> criteriaBuilder.lessThan(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.NOT_LESS_THAN -> criteriaBuilder.greaterThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.CONTAINS -> criteriaBuilder.like(path as Path<String>, "%${filter.value}%")
            ComparisonMethod.DOES_NOT_CONTAIN -> criteriaBuilder.notLike(path as Path<String>, "%${filter.value}%")
            ComparisonMethod.STARTS_WITH -> criteriaBuilder.like(path as Path<String>, "${filter.value}%")
            ComparisonMethod.DOES_NOT_START_WITH -> criteriaBuilder.notLike(path as Path<String>, "${filter.value}%")
            ComparisonMethod.ENDS_WITH -> criteriaBuilder.like(path as Path<String>, "%${filter.value}")
            ComparisonMethod.DOES_NOT_END_WITH -> criteriaBuilder.notLike(path as Path<String>, "%${filter.value}")
            ComparisonMethod.REGEX -> criteriaBuilder.like(path as Path<String>, filter.value.toString())
            ComparisonMethod.DOES_NOT_MATCH_REGEX -> criteriaBuilder.notLike(
                path as Path<String>,
                filter.value.toString()
            )

            ComparisonMethod.ENUM_EQUALS -> criteriaBuilder.equal(path as Path<Enum<*>>, filter.value)
            ComparisonMethod.ENUM_NOT_EQUALS -> criteriaBuilder.notEqual(path as Path<Enum<*>>, filter.value)
            ComparisonMethod.ENUM_IN -> {
                val values = filter.value as Collection<Enum<*>>
                criteriaBuilder.`in`(path as Path<Enum<*>>).apply { values.forEach { value(it) } }
            }

            ComparisonMethod.ENUM_NOT_IN -> {
                val values = filter.value as Collection<Enum<*>>
                criteriaBuilder.not(criteriaBuilder.`in`(path as Path<Enum<*>>).apply { values.forEach { value(it) } })
            }

            ComparisonMethod.BEFORE -> criteriaBuilder.lessThan(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.AFTER -> criteriaBuilder.greaterThan(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.NOT_BEFORE -> criteriaBuilder.greaterThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.NOT_AFTER -> criteriaBuilder.lessThanOrEqualTo(
                path as Path<Comparable<Any>>,
                filter.value as Comparable<Any>
            )

            ComparisonMethod.BETWEEN -> {
                val (start, end) = filter.value as Pair<Comparable<Any>, Comparable<Any>>
                criteriaBuilder.between(path as Path<Comparable<Any>>, start, end)
            }

            ComparisonMethod.NOT_BETWEEN -> {
                val (start, end) = filter.value as Pair<Comparable<Any>, Comparable<Any>>
                criteriaBuilder.not(criteriaBuilder.between(path as Path<Comparable<Any>>, start, end))
            }

            ComparisonMethod.IN -> criteriaBuilder.`in`(path).value(filter.value as Collection<*>)
            ComparisonMethod.NOT_IN -> criteriaBuilder.not(
                criteriaBuilder.`in`(path).value(filter.value as Collection<*>)
            )

            ComparisonMethod.CONTAINS_ALL, ComparisonMethod.DOES_NOT_CONTAIN_ALL,
            ComparisonMethod.CONTAINS_ANY, ComparisonMethod.DOES_NOT_CONTAIN_ANY -> {
                throw UnsupportedOperationException("Collection comparisons are not implemented.")
            }
            else -> throw UnsupportedOperationException("Unsupported comparison: ${filter.comparison}")
        }
    }
}