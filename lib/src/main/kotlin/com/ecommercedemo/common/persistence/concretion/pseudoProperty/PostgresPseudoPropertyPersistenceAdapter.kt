package com.ecommercedemo.common.persistence.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.PostgresEntityPersistenceAdapter
import org.springframework.stereotype.Service
import java.util.*

@Service
open class PostgresPseudoPropertyPersistenceAdapter(
    repository: EntityRepository<PseudoProperty, UUID>,
) : PostgresEntityPersistenceAdapter<PseudoProperty>(repository)