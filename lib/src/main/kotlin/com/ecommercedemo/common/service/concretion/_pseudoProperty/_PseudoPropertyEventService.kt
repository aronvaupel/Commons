package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
open class _PseudoPropertyEventService(
    adapter: IEntityPersistenceAdapter<_PseudoProperty>,
    serviceUtility: ServiceUtility<_PseudoProperty>,
    typeReAttacher: TypeReAttacher<_PseudoProperty>
): EventServiceTemplate<_PseudoProperty>(
    adapter,
    _PseudoProperty::class,
    serviceUtility,
    typeReAttacher
)