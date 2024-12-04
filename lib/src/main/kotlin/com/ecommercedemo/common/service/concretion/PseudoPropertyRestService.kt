package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
open class PseudoPropertyRestService(
    adapter: IEntityPersistenceAdapter<PseudoProperty>,
    eventProducer: EntityEventProducer,
    retriever: Retriever,
    serviceUtility: ServiceUtility,
) : RestServiceTemplate<PseudoProperty>(
    adapter,
    PseudoProperty::class,
    eventProducer,
    retriever,
    serviceUtility
)