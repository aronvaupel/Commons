package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyService
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.abstraction.EventServiceTemplate
import com.ecommercedemo.common.service.annotation.EventServiceFor
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Service
@EventServiceFor(_PseudoProperty::class)
@Conditional(ExcludeIfPseudoPropertyService::class)
open class _PseudoPropertyEventService: EventServiceTemplate<_PseudoProperty>()