package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import java.util.*

interface IPseudoPropertyRepository <T: BaseEntity>: EntityRepository<T, UUID> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<T>
}