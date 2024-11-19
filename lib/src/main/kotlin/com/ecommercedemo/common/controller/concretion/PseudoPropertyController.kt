package com.ecommercedemo.common.controller.concretion

import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.model.concretion.PseudoProperty
import com.ecommercedemo.common.service.concretion.PseudoPropertyService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pseudo-properties")
class PseudoPropertyController(
    pseudoPropertyService: PseudoPropertyService,
) : RestControllerTemplate<PseudoProperty>(pseudoPropertyService)