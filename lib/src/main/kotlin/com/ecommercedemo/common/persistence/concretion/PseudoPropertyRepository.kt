package com.ecommercedemo.common.persistence.concretion

import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository
import java.util.*

interface PseudoPropertyRepository : IPseudoPropertyRepository<PseudoProperty>, EntityRepository<PseudoProperty, UUID> {
    override fun findAllByEntitySimpleName(entitySimpleName: String): List<PseudoProperty>
}

