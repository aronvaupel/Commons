package com.ecommercedemo.common.persistence.concretion

import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository
import com.ecommercedemo.common.persistence.abstraction.PostgresEntityPersistenceAdapter
import org.springframework.stereotype.Service

@Service
open class PostgresPseudoPropertyPersistenceAdapter(
    repository: IPseudoPropertyRepository<PseudoProperty>,
) : PostgresEntityPersistenceAdapter<PseudoProperty>(repository)