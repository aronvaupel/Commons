package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.redis.RedisService
import com.ecommercedemo.common.validation.jpa.JPAPropertyValidator
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service


@Service
class PathResolver(
    private val redisService: RedisService,
    private val jpaValidation: JPAPropertyValidator,
) {

    fun <T : BaseEntity> resolve(
        filter: FilterCriteria,
        root: Root<T>
    ): Path<*> {
        validateFilterConfiguration(filter)

        return if (filter.value != null) {
            jpaValidation.validateAttribute(Class.forName(filter.entitySimpleName).kotlin, filter)
            root.get<String>(filter.jpaAttribute)
        } else resolveRecursively(filter.nestedFilter!!, root)
    }

    private fun resolveRecursively(
        nestedFilter: FilterCriteria,
        currentPath: Path<*>
    ): Path<*> {
        validateFilterConfiguration(nestedFilter)
        val extendedPath = currentPath.get<Any>(nestedFilter.jpaAttribute)
        return if (nestedFilter.value != null) {
            jpaValidation.validateAttribute(Class.forName(nestedFilter.entitySimpleName).kotlin, nestedFilter)
            extendedPath
        } else resolveRecursively(nestedFilter.nestedFilter!!, extendedPath)
    }

    private fun validateFilterConfiguration(filter: FilterCriteria) {
        if ((filter.value != null && filter.nestedFilter != null) ||
            (filter.value == null && filter.nestedFilter == null)
        ) {
            throw InvalidAttributeException(filter.jpaAttribute, filter.entitySimpleName)
        }
    }

}
