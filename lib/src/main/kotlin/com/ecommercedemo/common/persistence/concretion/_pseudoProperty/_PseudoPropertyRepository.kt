package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyService
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Conditional
import java.util.*

@Suppress("ClassName")
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
@Conditional(ExcludeIfPseudoPropertyService::class)
interface _PseudoPropertyRepository : EntityRepository<_PseudoProperty, UUID> {
    fun findAllByEntitySimpleName(entitySimpleName: String): List<_PseudoProperty>
}

