package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.RestServiceFor
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Suppress("ClassName")
@Transactional
@Service
@RestServiceFor(_PseudoProperty::class)
open class _PseudoPropertyRestService : RestServiceTemplate<_PseudoProperty>()