package com.ecommercedemo.common.persistence.concretion

import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository

interface PseudoPropertyRepository : IPseudoPropertyRepository<PseudoProperty> {
    override fun findAllByEntitySimpleName(entitySimpleName: String): List<PseudoProperty>
}

