package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Transactional
@Service
@RestServiceFor(_PseudoProperty::class)
@ConditionalOnProperty(name = ["pseudo-properties"], havingValue = "true", matchIfMissing = false)
open class _PseudoPropertyRestService : DownstreamRestServiceTemplate<_PseudoProperty>()