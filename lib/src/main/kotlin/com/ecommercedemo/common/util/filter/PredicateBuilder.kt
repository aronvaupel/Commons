package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.validation.comparison.ComparisonMethod
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component

@Component
@Suppress("UNCHECKED_CAST")
class PredicateBuilder(
    private val pathResolver: PathResolver,
) {

    fun <T: BaseEntity> build(criteriaBuilder: CriteriaBuilder, root: Root<T>, filter: FilterCriteria): Predicate {
        val attributePath = pathResolver.resolve(filter, root)
        val value = filter.value
        if (filter.comparison == null) throw IllegalArgumentException("Comparison method must be provided")
        else return  when (filter.comparison) {
                ComparisonMethod.EQUALS -> criteriaBuilder.equal(attributePath, value)
                ComparisonMethod.NOT_EQUALS -> criteriaBuilder.notEqual(attributePath, value)
                ComparisonMethod.CONTAINS -> criteriaBuilder.like(attributePath as Path<String>, "%$value%")
                ComparisonMethod.DOES_NOT_CONTAIN -> criteriaBuilder.notLike(attributePath as Path<String>, "%$value%")
                ComparisonMethod.STARTS_WITH -> criteriaBuilder.like(attributePath as Path<String>, "$value%")
                ComparisonMethod.DOES_NOT_START_WITH -> criteriaBuilder.notLike(attributePath as Path<String>, "$value%")
                ComparisonMethod.ENDS_WITH -> criteriaBuilder.like(attributePath as Path<String>, "%$value")
                ComparisonMethod.DOES_NOT_END_WITH -> criteriaBuilder.notLike(attributePath as Path<String>, "%$value")
                ComparisonMethod.REGEX -> criteriaBuilder.like(attributePath as Path<String>, value.toString())
                ComparisonMethod.DOES_NOT_MATCH_REGEX -> criteriaBuilder.notLike(attributePath as Path<String>, value.toString())
                ComparisonMethod.GREATER_THAN -> criteriaBuilder.greaterThan(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.LESS_THAN -> criteriaBuilder.lessThan(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.NOT_GREATER_THAN -> criteriaBuilder.lessThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.NOT_LESS_THAN -> criteriaBuilder.greaterThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.BEFORE -> criteriaBuilder.lessThan(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.AFTER -> criteriaBuilder.greaterThan(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.NOT_BEFORE -> criteriaBuilder.greaterThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.NOT_AFTER -> criteriaBuilder.lessThanOrEqualTo(attributePath as Path<Comparable<Any>>, value as Comparable<Any>)
                ComparisonMethod.BETWEEN -> criteriaBuilder.between(attributePath as Path<Comparable<Any>>, (value as List<Comparable<Any>>)[0], value[1])
                ComparisonMethod.NOT_BETWEEN -> criteriaBuilder.not(criteriaBuilder.between(attributePath as Path<Comparable<Any>>, (value as List<Comparable<Any>>)[0], value[1]))
                ComparisonMethod.ENUM_EQUALS -> criteriaBuilder.equal(attributePath, value)
                ComparisonMethod.ENUM_NOT_EQUALS -> criteriaBuilder.notEqual(attributePath, value)
                ComparisonMethod.ENUM_IN -> attributePath.`in`(value as Collection<*>)
                ComparisonMethod.ENUM_NOT_IN -> criteriaBuilder.not(attributePath.`in`(value as Collection<*>))
                ComparisonMethod.IN -> attributePath.`in`(value as Collection<*>)
                ComparisonMethod.NOT_IN -> criteriaBuilder.not(attributePath.`in`(value as Collection<*>))
                ComparisonMethod.CONTAINS_ALL -> criteriaBuilder.and(*(value as Collection<*>).map { criteriaBuilder.isMember(it, attributePath as Path<Collection<*>>) }.toTypedArray())
                ComparisonMethod.DOES_NOT_CONTAIN_ALL -> criteriaBuilder.not(criteriaBuilder.and(*(value as Collection<*>).map { criteriaBuilder.isMember(it, attributePath as Path<Collection<*>>) }.toTypedArray()))
                ComparisonMethod.CONTAINS_ANY -> criteriaBuilder.or(*(value as Collection<*>).map { criteriaBuilder.isMember(it, attributePath as Path<Collection<*>>) }.toTypedArray())
                ComparisonMethod.DOES_NOT_CONTAIN_ANY -> criteriaBuilder.not(criteriaBuilder.or(*(value as Collection<*>).map { criteriaBuilder.isMember(it, attributePath as Path<Collection<*>>) }.toTypedArray()))
        }
    }
}