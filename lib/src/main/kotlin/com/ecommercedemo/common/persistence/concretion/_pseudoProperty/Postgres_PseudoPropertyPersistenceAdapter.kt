package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.PostgresEntityPersistenceAdapter
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
class Postgres_PseudoPropertyPersistenceAdapter(
    repository: _PseudoPropertyRepository,
) : PostgresEntityPersistenceAdapter<_PseudoProperty>(repository)