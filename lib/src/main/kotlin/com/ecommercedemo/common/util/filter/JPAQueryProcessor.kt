package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.model.BaseEntity
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JPAQueryProcessor(
    @Autowired private val entityManager: EntityManager,
    @Autowired private val predicateBuilder: PredicateBuilder
) {

    fun <T : BaseEntity> process(filter: FilterCriteria, entityClass: Class<T>): List<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(entityClass)
        val root = query.from(entityClass)

        val predicate = predicateBuilder.build(criteriaBuilder, root, filter)
        query.where(predicate)

        return entityManager.createQuery(query).resultList
    }
}