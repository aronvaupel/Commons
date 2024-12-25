package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Lock
import java.util.*

abstract class EntityPersistenceAdapter<T : BaseEntity> : PersistencePort<T> {

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

    override fun getAllByIds(ids: List<UUID>, page: Int, size: Int): Page<T> {
        val pageable = PageRequest.of(page, size)
        return repository.findAllById(ids, pageable)
    }

    override fun getAllPaged(page: Int, size: Int): Page<T> {
        val pageable = PageRequest.of(page, size)
        return repository.findAll(pageable)
    }
}
