package com.ecommercedemo.common.controller.concretion

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import com.ecommercedemo.common.service.abstraction.RestServiceTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pseudo-properties")
class PseudoPropertyController(
    pseudoPropertyRestService: RestServiceTemplate<PseudoProperty>,
) : RestControllerTemplate<PseudoProperty>(pseudoPropertyRestService)