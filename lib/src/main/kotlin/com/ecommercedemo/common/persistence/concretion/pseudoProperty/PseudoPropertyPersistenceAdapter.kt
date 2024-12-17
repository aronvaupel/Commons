package com.ecommercedemo.common.persistence.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
open class PseudoPropertyPersistenceAdapter(
    repository: EntityRepository<PseudoProperty, UUID>,
) : EntityPersistenceAdapter<PseudoProperty>(repository)