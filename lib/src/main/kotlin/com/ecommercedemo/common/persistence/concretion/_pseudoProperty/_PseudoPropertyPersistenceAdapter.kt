package com.ecommercedemo.common.persistence.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyService
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import com.ecommercedemo.common.persistence.annotation.PersistenceAdapterFor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@PersistenceAdapterFor(_PseudoProperty::class)
@Conditional(ExcludeIfPseudoPropertyService::class)
class _PseudoPropertyPersistenceAdapter : EntityPersistenceAdapter<_PseudoProperty>()