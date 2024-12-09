package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.concretion.ServiceUtility
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
open class _PseudoPropertyEventService(
    adapter: IEntityPersistenceAdapter<_PseudoProperty>,
    serviceUtility: ServiceUtility
): EventServiceTemplate<_PseudoProperty>(
    adapter,
    serviceUtility,
    _PseudoProperty::class
)