package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.PersistenceAdapterFor
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Lock
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
abstract class EntityPersistenceAdapter<T : BaseEntity> : PersistencePort<T> {
    private var entityClass: KClass<T> = this::class.findAnnotation<PersistenceAdapterFor>()?.let { it.entity as KClass<T> }
        ?: throw IllegalStateException("No valid annotation found on class ${this::class.simpleName}")

    @Autowired
    private lateinit var repository: EntityRepository<T, UUID>

    @Autowired
    private lateinit var entityManager: EntityManager

    val log = KotlinLogging.logger {}

    override fun save(entity: T): T {
        val result = repository.save(entity) as T
        log.info { "Entity saved: $result" }
        return result
    }

    override fun saveAll(entities: List<T>): List<T> {
        val result = repository.saveAll(entities) as List<T>
        log.info { "Entities saved: $result" }
        return result
    }

    override fun delete(id: UUID) {
        try {
            repository.deleteById(id)
            log.info { "Entity with id $id deleted" }
        } catch (e: NoSuchElementException) {
            log.warn { "${e.message}" }
            log.debug { "${e.stackTrace}" }
        }

    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun getById(id: UUID): T {
        return repository.findById(id).orElseThrow { NoSuchElementException("Entity not found") }
    }

    override fun getAllByIds(ids: List<UUID>, page: Int , size: Int): Page<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entityClass.java)
        val root = criteriaQuery.from(entityClass.java)

        criteriaQuery.select(root).where(root.get<UUID>("id").`in`(ids))

        val query = entityManager.createQuery(criteriaQuery)
        query.firstResult = page * size
        query.maxResults = size
        val resultList = query.resultList

        val countQuery = criteriaBuilder.createQuery(Long::class.java)
        countQuery.select(criteriaBuilder.count(root)).where(root.get<UUID>("id").`in`(ids))
        val totalCount = entityManager.createQuery(countQuery).singleResult

        return PageImpl(resultList, PageRequest.of(page, size), totalCount)
    }

    override fun getAllPaged(page: Int, size: Int): Page<T> {
        val result = repository.findAll(PageRequest.of(page, size))
        log.info { "Entities fetched: $result" }
        return result
    }
}
