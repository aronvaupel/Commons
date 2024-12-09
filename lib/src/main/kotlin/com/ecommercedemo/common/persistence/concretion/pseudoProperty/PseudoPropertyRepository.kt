package com.ecommercedemo.common.persistence.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import java.util.*

@Suppress("unused")
interface PseudoPropertyRepository : EntityRepository<PseudoProperty, UUID>

