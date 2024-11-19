package com.ecommercedemo.common.persistence.concretion

import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import java.util.*

interface PseudoPropertyRepository : EntityRepository<PseudoProperty, UUID> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<PseudoProperty>
}

