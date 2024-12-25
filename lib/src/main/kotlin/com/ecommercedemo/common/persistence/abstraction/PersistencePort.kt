package com.ecommercedemo.common.persistence.abstraction

import org.springframework.data.domain.Page
import java.util.*

interface PersistencePort<T> {
    fun save(entity: T): T
    fun saveAll(entities: List<T>): List<T>
    fun delete(id: UUID)
    fun getById(id: UUID): T
    fun getAllByIds(ids: List<UUID>, page: Int, size: Int): Page<T>
    fun getAllPaged(page: Int, size: Int): Page<T>
}