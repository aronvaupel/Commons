package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.util.*

abstract class EntityPersistenceAdapter<T : BaseEntity>(
    private val repository: EntityRepository<T, UUID>
): IEntityPersistenceAdapter<T> {
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

    override fun getById(id: UUID): T {
        return repository.findById(id).orElseThrow() as T
    }

    override fun getAllByIds(ids: List<UUID>): List<T> {
        return repository.findAllById(ids) as List<T>
    }

    override fun getAllByIdsWithLock(ids: List<UUID>): List<T> {
        return repository.findAllByIdForUpdate(ids)
    }

    override fun getAllPaged(page: Int, size: Int): Page<T> {
        val pageable = PageRequest.of(page, size)
        return repository.findAll(pageable)
    }

    override fun getByIdWithLock(id: UUID): T {
        return repository.findByIdForUpdate(id)
    }
}
