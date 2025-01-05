package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@PersistenceAdapterFor(_PseudoProperty::class)
@ConditionalOnProperty(name = ["pseudo-properties"], havingValue = "true", matchIfMissing = false)
class _PseudoPropertyPersistenceAdapter : EntityPersistenceAdapter<_PseudoProperty>()