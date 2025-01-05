package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyService
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Transactional
@Service
@RestServiceFor(_PseudoProperty::class)
@Conditional(ExcludeIfPseudoPropertyService::class)
open class _PseudoPropertyRestService : DownstreamRestServiceTemplate<_PseudoProperty>()