package com.ecommercedemo.common.controller.concretion

import com.ecommercedemo.common.controller.ControllerFor
import com.ecommercedemo.common.controller.abstraction.RestControllerTemplate
import com.ecommercedemo.common.model.concretion.pseudoProperty.PseudoProperty
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pseudo-properties")
@ControllerFor(PseudoProperty::class)
class PseudoPropertyController: RestControllerTemplate<PseudoProperty>()