package com.ecommercedemo.common.service.concretion.pseudoProperty

import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import com.ecommercedemo.common.service.abstraction.DownstreamRestServiceTemplate
import com.ecommercedemo.common.service.annotation.RestServiceFor
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Service

@Transactional
@Service
@RestServiceFor(_PseudoProperty::class)
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
@Suppress("ClassName")
open class _PseudoPropertyRestService: DownstreamRestServiceTemplate<_PseudoProperty>()