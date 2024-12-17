package com.ecommercedemo.common.service.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.service.RestServiceFor
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Transactional
@Service
@RestServiceFor(PseudoProperty::class)
open class PseudoPropertyRestService(
) : RestServiceTemplate<PseudoProperty>()