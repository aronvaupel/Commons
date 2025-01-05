package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@EventServiceFor(_PseudoProperty::class)
@ConditionalOnProperty(name = ["pseudo-properties"], havingValue = "true", matchIfMissing = false)
open class _PseudoPropertyEventService: EventServiceTemplate<_PseudoProperty>()