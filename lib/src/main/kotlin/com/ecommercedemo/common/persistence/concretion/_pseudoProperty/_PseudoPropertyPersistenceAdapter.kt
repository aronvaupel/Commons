package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.PersistenceAdapterFor
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@PersistenceAdapterFor(_PseudoProperty::class)
class _PseudoPropertyPersistenceAdapter : EntityPersistenceAdapter<_PseudoProperty>()