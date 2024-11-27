package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.persistence.concretion.PseudoPropertyRepository
import com.ecommercedemo.common.service.abstraction.ServiceTemplate
import com.ecommercedemo.common.service.utility.ServiceUtility
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
open class PseudoPropertyService(
    adapter: IEntityPersistenceAdapter<PseudoProperty>,
    eventProducer: EntityEventProducer,
    objectMapper: ObjectMapper,
    pseudoPropertyRepository: PseudoPropertyRepository,
    retriever: Retriever,
    utility: ServiceUtility<PseudoProperty>
) : ServiceTemplate<PseudoProperty>(
    adapter,
    PseudoProperty::class,
    eventProducer,
    objectMapper,
    pseudoPropertyRepository,
    retriever,
    utility
)