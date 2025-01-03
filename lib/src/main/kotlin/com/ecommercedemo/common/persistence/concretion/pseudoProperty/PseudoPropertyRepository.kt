package com.ecommercedemo.common.persistence.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import java.util.*

@Suppress("unused")
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
interface PseudoPropertyRepository : EntityRepository<PseudoProperty, UUID>

