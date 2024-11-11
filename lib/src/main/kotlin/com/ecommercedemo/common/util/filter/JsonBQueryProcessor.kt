package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.model.BaseEntity
import jakarta.persistence.EntityManager

class JsonBQueryProcessor(
    private val entityManager: EntityManager
) {
    fun <T : BaseEntity> process(filter: FilterCriteria, entity: Class<T>): List<T> {
        val queryBuilder = JsonBQueryBuilder(filter)
        val sqlQuery = queryBuilder.buildQuery(entity)

        return entityManager.createNativeQuery(sqlQuery, entity).resultList as List<T>
    }
}