package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.util.*

@Suppress("ClassName")
@ConditionalOnProperty(name = ["pseudo-properties"], havingValue = "true", matchIfMissing = false)
interface _PseudoPropertyRepository : EntityRepository<_PseudoProperty, UUID> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<_PseudoProperty>
}

