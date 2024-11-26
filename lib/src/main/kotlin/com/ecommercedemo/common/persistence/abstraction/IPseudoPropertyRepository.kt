package com.ecommercedemo.common.persistence.abstraction

import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import java.util.*

interface IPseudoPropertyRepository <T: BasePseudoProperty>: EntityRepository<T, UUID> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<T>
}