package com.ecommercedemo.common.persistence.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.PersistenceAdapterFor
import com.ecommercedemo.common.persistence.abstraction.EntityPersistenceAdapter
import org.springframework.stereotype.Service

@Service
@PersistenceAdapterFor(PseudoProperty::class)
open class PseudoPropertyPersistenceAdapter : EntityPersistenceAdapter<PseudoProperty>()