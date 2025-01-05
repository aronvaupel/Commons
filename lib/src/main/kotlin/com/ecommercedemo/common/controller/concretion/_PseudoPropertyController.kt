package com.ecommercedemo.common.controller.concretion

import com.ecommercedemo.common.application.condition.ExcludeIfPseudoPropertyService
import com.ecommercedemo.common.controller.abstraction.DownstreamRestControllerTemplate
import com.ecommercedemo.common.controller.annotation.ControllerFor
import com.ecommercedemo.common.model.concretion._pseudoProperty._PseudoProperty
import org.springframework.context.annotation.Conditional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pseudo-properties")
@ControllerFor(_PseudoProperty::class)
@Conditional(ExcludeIfPseudoPropertyService::class)
@Suppress("ClassName")
class _PseudoPropertyController: DownstreamRestControllerTemplate<_PseudoProperty>()