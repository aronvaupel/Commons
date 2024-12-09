package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import java.util.*

abstract class PostgresEntityPersistenceAdapter<T : BaseEntity>(
    private val repository: EntityRepository<T, UUID>
): IEntityPersistenceAdapter<T> {
    override fun save(entity: T): T {
        return repository.save(entity) as T
    }

    override fun delete(id: UUID) {
        repository.deleteById(id)
    }

    override fun getById(id: UUID): T {
        return repository.findById(id).orElseThrow() as T
    }

    override fun getAllByIds(ids: List<UUID>): List<T> {
        return repository.findAllById(ids) as List<T>
    }
}
