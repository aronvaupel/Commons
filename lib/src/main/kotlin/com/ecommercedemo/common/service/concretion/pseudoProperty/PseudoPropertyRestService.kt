package com.ecommercedemo.common.service.concretion.pseudoProperty

import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import com.ecommercedemo.common.service.concretion.ServiceUtility
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
open class PseudoPropertyRestService(
    adapter: IEntityPersistenceAdapter<PseudoProperty>,
    entityManager: EntityManager,
    eventProducer: EntityEventProducer,
    retriever: Retriever,
    serviceUtility: ServiceUtility<PseudoProperty>,
) : RestServiceTemplate<PseudoProperty>(
    adapter,
    PseudoProperty::class,
    entityManager,
    eventProducer,
    retriever,
    serviceUtility
)