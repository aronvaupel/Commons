package com.ecommercedemo.common.persistence.abstraction

import java.util.*

interface IEntityPersistenceAdapter<T> {
    fun save(entity: T): T
    fun delete(id: UUID)
    fun getById(id: UUID): T
    fun getAllByIds(ids: List<UUID>): List<T>
}