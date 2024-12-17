package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
class _PseudoPropertyPersistenceAdapter(
    repository: _PseudoPropertyRepository,
) : EntityPersistenceAdapter<_PseudoProperty>(repository)